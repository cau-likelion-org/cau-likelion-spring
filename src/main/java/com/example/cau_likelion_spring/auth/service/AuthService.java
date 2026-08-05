package com.example.cau_likelion_spring.auth.service;

import com.example.cau_likelion_spring.auth.domain.RefreshToken;
import com.example.cau_likelion_spring.auth.dto.OAuthLoginResult;
import com.example.cau_likelion_spring.auth.dto.RefreshTokenRequest;
import com.example.cau_likelion_spring.auth.dto.JoinRequest;
import com.example.cau_likelion_spring.auth.dto.TokenResponse;
import com.example.cau_likelion_spring.auth.repository.RefreshTokenRepository;
import com.example.cau_likelion_spring.global.exception.CustomException;
import com.example.cau_likelion_spring.global.exception.ErrorCode;
import com.example.cau_likelion_spring.global.jwt.JwtTokenProvider;
import com.example.cau_likelion_spring.global.jwt.JwtValidationType;
import com.example.cau_likelion_spring.member.domain.AllowedUserEmail;
import com.example.cau_likelion_spring.member.domain.Member;
import com.example.cau_likelion_spring.member.domain.MemberRole;
import com.example.cau_likelion_spring.member.repository.AllowedUserEmailRepository;
import com.example.cau_likelion_spring.member.repository.MemberRepository;
import com.example.cau_likelion_spring.organization.domain.Part;
import com.example.cau_likelion_spring.organization.repository.PartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final MemberRepository memberRepository;
    private final AllowedUserEmailRepository allowedUserEmailRepository;
    private final PartRepository partRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public TokenResponse join(JoinRequest request) {
        if (jwtTokenProvider.validateToken(request.signupToken()) != JwtValidationType.VALID_JWT
                || !jwtTokenProvider.isSignupToken(request.signupToken())) {
            throw new CustomException(ErrorCode.INVALID_TOKEN, "유효하지 않거나 만료된 가입 토큰입니다. 구글 로그인부터 다시 진행해주세요.");
        }
        String email = jwtTokenProvider.getEmail(request.signupToken());

        AllowedUserEmail allowedUserEmail = allowedUserEmailRepository
                .findByAllowedEmailAndGeneration_Id(email, request.generationId())
                .orElseThrow(() -> new CustomException(ErrorCode.EMAIL_NOT_ALLOWED, "가입하실 이메일 주소를 다시 확인해주세요."));

        Part part = partRepository.findById(request.partId())
                .orElseThrow(() -> new CustomException(ErrorCode.PART_NOT_FOUND, "존재하지 않는 파트입니다. id=" + request.partId()));

        Member member = memberRepository.save(Member.builder()
                .name(request.name())
                .email(email)
                .role(MemberRole.BABY_LION)
                .part(part)
                .build());

        // 가입 완료됐으니 예비 회원 등록 레코드는 정리한다
        allowedUserEmailRepository.delete(allowedUserEmail);

        return issueTokens(member);
    }

    /**
     * 구글 로그인 콜백에서 호출된다. email은 구글이 검증해 준 값이라 그대로 신뢰한다.
     * 기존 회원이면 바로 로그인 처리, 처음이면 사전등록 여부에 따라 가입 폼으로 보낼지 에러를 낼지 판단한다.
     */
    @Transactional
    public OAuthLoginResult processOAuthLogin(String email) {
        return memberRepository.findByEmail(email)
                .map(member -> (OAuthLoginResult) new OAuthLoginResult.LoginSuccess(issueTokens(member)))
                .orElseGet(() -> {
                    if (!allowedUserEmailRepository.existsByAllowedEmail(email)) {
                        throw new CustomException(ErrorCode.EMAIL_NOT_ALLOWED, "가입하실 이메일 주소를 다시 확인해주세요.");
                    }
                    return new OAuthLoginResult.SignupRequired(jwtTokenProvider.createSignupToken(email));
                });
    }

    @Transactional
    public TokenResponse reissue(RefreshTokenRequest request) {
        String refreshToken = request.refreshToken();

        if (jwtTokenProvider.validateToken(refreshToken) != JwtValidationType.VALID_JWT) {
            throw new CustomException(ErrorCode.INVALID_TOKEN, "유효하지 않은 토큰입니다.");
        }

        RefreshToken savedRefreshToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_TOKEN, "유효하지 않은 토큰입니다."));

        Member member = savedRefreshToken.getMember();
        String newAccessToken = jwtTokenProvider.createAccessToken(member.getId(), member.getRole());

        return new TokenResponse(newAccessToken, refreshToken);
    }

    @Transactional
    public void logout(RefreshTokenRequest request) {
        refreshTokenRepository.deleteByToken(request.refreshToken());
    }

    private TokenResponse issueTokens(Member member) {
        String accessToken = jwtTokenProvider.createAccessToken(member.getId(), member.getRole());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getId());

        refreshTokenRepository.deleteByMember_Id(member.getId());
        refreshTokenRepository.save(RefreshToken.builder()
                .member(member)
                .token(refreshToken)
                .expiryDate(LocalDateTime.now().plus(jwtTokenProvider.getRefreshTokenExpiration()))
                .build());

        return new TokenResponse(accessToken, refreshToken);
    }
}

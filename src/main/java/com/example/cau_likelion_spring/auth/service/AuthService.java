package com.example.cau_likelion_spring.auth.service;

import com.example.cau_likelion_spring.auth.domain.RefreshToken;
import com.example.cau_likelion_spring.auth.dto.LoginRequest;
import com.example.cau_likelion_spring.auth.dto.RefreshTokenRequest;
import com.example.cau_likelion_spring.auth.dto.JoinRequest;
import com.example.cau_likelion_spring.auth.dto.TokenResponse;
import com.example.cau_likelion_spring.auth.repository.RefreshTokenRepository;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
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
        AllowedUserEmail allowedUserEmail = allowedUserEmailRepository
                .findByAllowedEmailAndGeneration_IdAndIsJoinedFalse(request.email(), request.generationId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "가입하실 이메일 주소를 다시 확인해주세요."));

        Part part = partRepository.findById(request.partId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 파트입니다."));

        allowedUserEmail.markAsJoined();

        Member member = memberRepository.save(Member.builder()
                .name(request.name())
                .email(request.email())
                .role(MemberRole.BABY_LION)
                .part(part)
                .build());

        return issueTokens(member);
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "회원가입한 이메일로만 로그인이 가능합니다."));

        return issueTokens(member);
    }

    @Transactional
    public TokenResponse reissue(RefreshTokenRequest request) {
        String refreshToken = request.refreshToken();

        if (jwtTokenProvider.validateToken(refreshToken) != JwtValidationType.VALID_JWT) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다.");
        }

        RefreshToken savedRefreshToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."));

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
                .expiryDate(LocalDateTime.now().plus(Duration.ofMillis(jwtTokenProvider.getRefreshTokenExpiration())))
                .build());

        return new TokenResponse(accessToken, refreshToken);
    }
}

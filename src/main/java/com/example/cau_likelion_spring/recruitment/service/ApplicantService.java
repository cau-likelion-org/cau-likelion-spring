package com.example.cau_likelion_spring.recruitment.service;

import com.example.cau_likelion_spring.global.exception.CustomException;
import com.example.cau_likelion_spring.global.exception.ErrorCode;
import com.example.cau_likelion_spring.global.jwt.JwtTokenProvider;
import com.example.cau_likelion_spring.global.jwt.JwtValidationType;
import com.example.cau_likelion_spring.recruitment.domain.Applicant;
import com.example.cau_likelion_spring.recruitment.domain.EmailVerificationPurpose;
import com.example.cau_likelion_spring.recruitment.domain.Round;
import com.example.cau_likelion_spring.recruitment.dto.ApplicantLoginRequest;
import com.example.cau_likelion_spring.recruitment.dto.ApplicantLoginResponse;
import com.example.cau_likelion_spring.recruitment.dto.ApplicantMeResponse;
import com.example.cau_likelion_spring.recruitment.dto.ApplicantPasswordResetRequest;
import com.example.cau_likelion_spring.recruitment.dto.ApplicantSignupRequest;
import com.example.cau_likelion_spring.recruitment.dto.ApplicantSignupResponse;
import com.example.cau_likelion_spring.recruitment.repository.ApplicantRepository;
import com.example.cau_likelion_spring.recruitment.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApplicantService {

    private final ApplicantRepository applicantRepository;
    private final ApplicationRepository applicationRepository;
    private final CurrentRoundFinder currentRoundFinder;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    /**
     * 0단계 지원자 계정을 만든다 (7.3.1). 접수 기간에만 만들 수 있고, 현재 회차에 묶인다.
     * 만든 시각을 폼 진입 시각으로 기록하고(지각 제출 판정용), 바로 1단계로 넘어갈 수 있게 지원자 토큰을 발급한다.
     */
    @Transactional
    public ApplicantSignupResponse signup(ApplicantSignupRequest request) {
        String email = request.getEmail().toLowerCase(Locale.ROOT);
        validateVerificationToken(request.getVerificationToken(), email);

        LocalDateTime now = LocalDateTime.now();
        Round round = currentRoundFinder.find();
        if (!isInApplyPeriod(round, now)) {
            throw new CustomException(ErrorCode.RECRUITMENT_NOT_IN_APPLY_PERIOD);
        }
        validateEmailAvailable(round, email);

        Applicant applicant;
        try {
            applicant = applicantRepository.saveAndFlush(Applicant.builder()
                    .round(round)
                    .name(request.getName())
                    .phone(request.getPhone())
                    .email(email)
                    .password(passwordEncoder.encode(request.getPassword()))
                    .formEnteredAt(now)
                    .build());
        } catch (DataIntegrityViolationException e) {
            // 같은 이메일의 계정 생성 요청이 동시에 들어와 다른 요청이 먼저 저장한 경우
            throw new CustomException(ErrorCode.RECRUITMENT_APPLICATION_IN_PROGRESS);
        }
        return ApplicantSignupResponse.of(jwtTokenProvider.createApplicantToken(applicant.getId()));
    }

    /**
     * 현재 회차 계정으로 로그인하고 지원자 토큰을 발급한다 (7.3.2).
     * 가입 여부를 알려주지 않도록 이메일이 없을 때와 비밀번호가 틀릴 때를 같은 오류로 응답한다.
     * 접수 기간 안의 로그인일 때만 폼 진입 시각을 갱신한다 (마감 후 로그인으로 지각 제출 자격이 바뀌지 않도록).
     */
    @Transactional
    public ApplicantLoginResponse login(ApplicantLoginRequest request) {
        String email = request.getEmail().toLowerCase(Locale.ROOT);
        Round round = currentRoundFinder.find();
        Applicant applicant = applicantRepository.findByRoundAndEmail(round, email)
                .filter(found -> passwordEncoder.matches(request.getPassword(), found.getPassword()))
                .orElseThrow(() -> new CustomException(ErrorCode.RECRUITMENT_LOGIN_FAILED));

        LocalDateTime now = LocalDateTime.now();
        if (isInApplyPeriod(round, now)) {
            applicant.updateFormEnteredAt(now);
        }
        return ApplicantLoginResponse.of(jwtTokenProvider.createApplicantToken(applicant.getId()));
    }

    /**
     * 이메일 인증을 마친 지원자의 비밀번호를 바꾼다 (7.3.4). 대상은 인증 완료 토큰에 담긴 이메일의 현재 회차 계정이다.
     * 가입 여부를 알려주지 않도록, 계정이 없어도 오류 없이 끝낸다.
     * 마감 후에도 결과 확인을 위해 로그인해야 하므로 접수 기간은 확인하지 않는다.
     */
    @Transactional
    public void resetPassword(ApplicantPasswordResetRequest request) {
        String token = request.getVerificationToken();
        if (!isVerificationToken(token, EmailVerificationPurpose.PASSWORD_RESET)) {
            throw new CustomException(ErrorCode.RECRUITMENT_INVALID_VERIFICATION_TOKEN);
        }
        applicantRepository.findByRoundAndEmail(currentRoundFinder.find(), jwtTokenProvider.getEmail(token))
                .ifPresent(applicant -> applicant.updatePassword(passwordEncoder.encode(request.getNewPassword())));
    }

    /** 1단계에서 이름·전화번호·이메일을 수정 불가로 보여주기 위한 본인 정보 조회 */
    public ApplicantMeResponse getMe(Long applicantId) {
        Applicant applicant = applicantRepository.findById(applicantId)
                .orElseThrow(() -> new CustomException(ErrorCode.RECRUITMENT_APPLICANT_NOT_FOUND));
        return ApplicantMeResponse.from(applicant);
    }

    /**
     * 해당 회차에 이 이메일로 가입할 수 있는지 확인한다.
     * 이미 계정이 있으면 지원서 제출 여부에 따라 다른 메시지로 거절한다.
     */
    public void validateEmailAvailable(Round round, String email) {
        applicantRepository.findByRoundAndEmail(round, email).ifPresent(applicant -> {
            if (applicationRepository.existsByApplicantAndRoundPart_Round(applicant, round)) {
                throw new CustomException(ErrorCode.RECRUITMENT_ALREADY_APPLIED);
            }
            throw new CustomException(ErrorCode.RECRUITMENT_APPLICATION_IN_PROGRESS);
        });
    }

    private boolean isInApplyPeriod(Round round, LocalDateTime now) {
        return round.getStartDate() != null && round.getDeadLine() != null
                && !now.isBefore(round.getStartDate()) && !now.isAfter(round.getDeadLine());
    }

    /** 가입 용도로 이 이메일을 인증하고 받은 토큰인지 확인한다 */
    private void validateVerificationToken(String token, String email) {
        boolean valid = isVerificationToken(token, EmailVerificationPurpose.SIGNUP)
                && email.equals(jwtTokenProvider.getEmail(token));
        if (!valid) {
            throw new CustomException(ErrorCode.RECRUITMENT_INVALID_VERIFICATION_TOKEN);
        }
    }

    /** 해당 용도로 이메일 인증을 마치고 받은, 아직 유효한 토큰인지 확인한다 */
    private boolean isVerificationToken(String token, EmailVerificationPurpose purpose) {
        return jwtTokenProvider.validateToken(token) == JwtValidationType.VALID_JWT
                && jwtTokenProvider.isEmailVerificationToken(token)
                && purpose.name().equals(jwtTokenProvider.getPurpose(token));
    }
}

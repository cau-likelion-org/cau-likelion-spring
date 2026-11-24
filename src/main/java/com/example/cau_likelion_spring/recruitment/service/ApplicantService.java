package com.example.cau_likelion_spring.recruitment.service;

import com.example.cau_likelion_spring.global.exception.CustomException;
import com.example.cau_likelion_spring.global.exception.ErrorCode;
import com.example.cau_likelion_spring.global.jwt.JwtTokenProvider;
import com.example.cau_likelion_spring.global.jwt.JwtValidationType;
import com.example.cau_likelion_spring.recruitment.domain.Applicant;
import com.example.cau_likelion_spring.recruitment.domain.EmailVerificationPurpose;
import com.example.cau_likelion_spring.recruitment.domain.Round;
import com.example.cau_likelion_spring.recruitment.dto.ApplicantMeResponse;
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
        if (round.getStartDate() == null || round.getDeadLine() == null
                || now.isBefore(round.getStartDate()) || now.isAfter(round.getDeadLine())) {
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

    /** 가입 용도로 이 이메일을 인증하고 받은 토큰인지 확인한다 */
    private void validateVerificationToken(String token, String email) {
        boolean valid = jwtTokenProvider.validateToken(token) == JwtValidationType.VALID_JWT
                && jwtTokenProvider.isEmailVerificationToken(token)
                && EmailVerificationPurpose.SIGNUP.name().equals(jwtTokenProvider.getPurpose(token))
                && email.equals(jwtTokenProvider.getEmail(token));
        if (!valid) {
            throw new CustomException(ErrorCode.RECRUITMENT_INVALID_VERIFICATION_TOKEN);
        }
    }
}

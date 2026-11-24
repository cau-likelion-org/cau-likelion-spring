package com.example.cau_likelion_spring.recruitment.service;

import com.example.cau_likelion_spring.global.exception.CustomException;
import com.example.cau_likelion_spring.global.exception.ErrorCode;
import com.example.cau_likelion_spring.global.jwt.JwtTokenProvider;
import com.example.cau_likelion_spring.recruitment.domain.EmailVerification;
import com.example.cau_likelion_spring.recruitment.domain.EmailVerificationPurpose;
import com.example.cau_likelion_spring.recruitment.dto.EmailVerificationConfirmRequest;
import com.example.cau_likelion_spring.recruitment.dto.EmailVerificationConfirmResponse;
import com.example.cau_likelion_spring.recruitment.dto.EmailVerificationSendRequest;
import com.example.cau_likelion_spring.recruitment.dto.EmailVerificationSendResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final EmailVerificationStore emailVerificationStore;
    private final EmailVerificationMailSender emailVerificationMailSender;
    private final JwtTokenProvider jwtTokenProvider;
    private final CurrentRoundFinder currentRoundFinder;
    private final ApplicantService applicantService;

    @Value("${recruitment.email-verification.code-ttl}")
    private Duration codeTtl;

    @Value("${recruitment.email-verification.resend-interval}")
    private Duration resendInterval;

    @Value("${recruitment.email-verification.max-fail-count}")
    private int maxFailCount;

    /**
     * 6자리 인증번호를 저장하고 메일로 보낸다 (7.3.1, 7.3.4).
     * 메일 발송이 실패하면 저장도 롤백되어, 재발송 간격 제한에 걸리지 않고 바로 다시 요청할 수 있다.
     */
    @Transactional
    public EmailVerificationSendResponse send(EmailVerificationSendRequest request) {
        String email = normalize(request.getEmail());
        EmailVerificationPurpose purpose = request.getPurpose();
        LocalDateTime now = LocalDateTime.now();

        // 이미 가입한 이메일은 인증번호를 보내기 전에 알려준다 (계정 생성 단계에서도 다시 확인)
        if (purpose == EmailVerificationPurpose.SIGNUP) {
            applicantService.validateEmailAvailable(currentRoundFinder.find(), email);
        }

        Optional<EmailVerification> existing = emailVerificationStore.find(email, purpose);
        if (existing.isPresent() && now.isBefore(existing.get().getSentAt().plus(resendInterval))) {
            throw new CustomException(ErrorCode.RECRUITMENT_EMAIL_RESEND_TOO_SOON);
        }

        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        LocalDateTime expiresAt = now.plus(codeTtl);

        EmailVerification verification = existing
                .map(found -> {
                    found.renew(code, expiresAt, now);
                    return found;
                })
                .orElseGet(() -> EmailVerification.builder()
                        .email(email)
                        .purpose(purpose)
                        .code(code)
                        .expiresAt(expiresAt)
                        .sentAt(now)
                        .build());
        try {
            emailVerificationStore.save(verification);
        } catch (DataIntegrityViolationException e) {
            // 같은 이메일·용도의 첫 발송 요청이 동시에 들어와 다른 요청이 먼저 저장한 경우
            throw new CustomException(ErrorCode.RECRUITMENT_EMAIL_RESEND_TOO_SOON);
        }

        emailVerificationMailSender.send(email, code, codeTtl);
        return EmailVerificationSendResponse.of(expiresAt);
    }

    /**
     * 인증번호를 확인하고, 맞으면 인증 완료 토큰을 발급한다.
     * 한 트랜잭션으로 묶지 않는다: 묶으면 불일치 예외로 롤백되면서 시도 횟수 증가까지 취소된다.
     */
    public EmailVerificationConfirmResponse confirm(EmailVerificationConfirmRequest request) {
        String email = normalize(request.getEmail());
        EmailVerificationPurpose purpose = request.getPurpose();

        EmailVerification verification = emailVerificationStore.find(email, purpose)
                .filter(found -> LocalDateTime.now().isBefore(found.getExpiresAt()))
                .orElseThrow(() -> new CustomException(ErrorCode.RECRUITMENT_EMAIL_CODE_EXPIRED));

        // 비교 전에 시도 횟수부터 올려서, 동시에 여러 번 시도해도 최대 횟수까지만 비교되게 한다
        if (!emailVerificationStore.countAttempt(verification.getId(), maxFailCount)) {
            throw new CustomException(ErrorCode.RECRUITMENT_EMAIL_CODE_ATTEMPTS_EXCEEDED);
        }
        if (!matches(verification.getCode(), request.getCode())) {
            throw new CustomException(ErrorCode.RECRUITMENT_EMAIL_CODE_MISMATCH);
        }

        emailVerificationStore.delete(verification.getId());
        return EmailVerificationConfirmResponse.of(
                jwtTokenProvider.createEmailVerificationToken(email, purpose.name()));
    }

    private String normalize(String email) {
        return email.toLowerCase(Locale.ROOT);
    }

    private boolean matches(String expected, String actual) {
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8), actual.getBytes(StandardCharsets.UTF_8));
    }
}

package com.example.cau_likelion_spring.recruitment.service;

import com.example.cau_likelion_spring.global.exception.CustomException;
import com.example.cau_likelion_spring.global.exception.ErrorCode;
import com.example.cau_likelion_spring.global.jwt.JwtTokenProvider;
import com.example.cau_likelion_spring.recruitment.domain.EmailVerification;
import com.example.cau_likelion_spring.recruitment.domain.EmailVerificationPurpose;
import com.example.cau_likelion_spring.recruitment.dto.EmailVerificationConfirmRequest;
import com.example.cau_likelion_spring.recruitment.dto.EmailVerificationSendRequest;
import com.example.cau_likelion_spring.recruitment.dto.EmailVerificationSendResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    private static final String EMAIL = "applicant@cau.ac.kr";
    private static final EmailVerificationPurpose PURPOSE = EmailVerificationPurpose.SIGNUP;
    private static final Duration CODE_TTL = Duration.ofMinutes(10);
    private static final int MAX_FAIL_COUNT = 5;

    @Mock
    private EmailVerificationStore emailVerificationStore;

    @Mock
    private EmailVerificationMailSender emailVerificationMailSender;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private EmailVerificationService emailVerificationService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailVerificationService, "codeTtl", CODE_TTL);
        ReflectionTestUtils.setField(emailVerificationService, "resendInterval", Duration.ofSeconds(60));
        ReflectionTestUtils.setField(emailVerificationService, "maxFailCount", MAX_FAIL_COUNT);
    }

    @Test
    void 처음_발송하면_6자리_인증번호를_저장하고_메일을_보낸다() {
        given(emailVerificationStore.find(EMAIL, PURPOSE)).willReturn(Optional.empty());

        EmailVerificationSendResponse response = emailVerificationService.send(sendRequest("Applicant@CAU.ac.kr"));

        ArgumentCaptor<EmailVerification> saved = ArgumentCaptor.forClass(EmailVerification.class);
        verify(emailVerificationStore).save(saved.capture());
        assertThat(saved.getValue().getEmail()).isEqualTo(EMAIL);
        assertThat(saved.getValue().getCode()).matches("\\d{6}");
        assertThat(saved.getValue().getExpiresAt()).isEqualTo(saved.getValue().getSentAt().plus(CODE_TTL));
        assertThat(response.getExpiresAt()).isEqualTo(saved.getValue().getExpiresAt());
        verify(emailVerificationMailSender).send(EMAIL, saved.getValue().getCode(), CODE_TTL);
    }

    @Test
    void 발송_후_60초가_지나지_않았으면_재발송을_거절한다() {
        LocalDateTime sentAt = LocalDateTime.now().minusSeconds(30);
        given(emailVerificationStore.find(EMAIL, PURPOSE))
                .willReturn(Optional.of(verification("123456", sentAt.plus(CODE_TTL), sentAt)));

        assertThatThrownBy(() -> emailVerificationService.send(sendRequest(EMAIL)))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.RECRUITMENT_EMAIL_RESEND_TOO_SOON);

        verify(emailVerificationStore, never()).save(any());
        verify(emailVerificationMailSender, never()).send(any(), any(), any());
    }

    @Test
    void 발송_후_60초가_지났으면_새_인증번호로_덮어쓰고_다시_보낸다() {
        LocalDateTime sentAt = LocalDateTime.now().minusSeconds(61);
        EmailVerification existing = verification("123456", sentAt.plus(CODE_TTL), sentAt);
        given(emailVerificationStore.find(EMAIL, PURPOSE)).willReturn(Optional.of(existing));

        emailVerificationService.send(sendRequest(EMAIL));

        verify(emailVerificationStore).save(existing);
        assertThat(existing.getSentAt()).isAfter(sentAt);
        assertThat(existing.getFailCount()).isZero();
        verify(emailVerificationMailSender).send(EMAIL, existing.getCode(), CODE_TTL);
    }

    @Test
    void 요청한_인증번호가_없으면_만료로_처리한다() {
        given(emailVerificationStore.find(EMAIL, PURPOSE)).willReturn(Optional.empty());

        assertConfirmFails("123456", ErrorCode.RECRUITMENT_EMAIL_CODE_EXPIRED);
    }

    @Test
    void 유효시간이_지난_인증번호는_만료로_처리하고_시도_횟수를_올리지_않는다() {
        LocalDateTime sentAt = LocalDateTime.now().minusMinutes(11);
        given(emailVerificationStore.find(EMAIL, PURPOSE))
                .willReturn(Optional.of(verification("123456", sentAt.plus(CODE_TTL), sentAt)));

        assertConfirmFails("123456", ErrorCode.RECRUITMENT_EMAIL_CODE_EXPIRED);
        verify(emailVerificationStore, never()).countAttempt(anyLong(), eq(MAX_FAIL_COUNT));
    }

    @Test
    void 시도_횟수를_다_쓴_인증번호는_번호가_맞아도_거절한다() {
        givenValidVerification("123456");
        given(emailVerificationStore.countAttempt(1L, MAX_FAIL_COUNT)).willReturn(false);

        assertConfirmFails("123456", ErrorCode.RECRUITMENT_EMAIL_CODE_ATTEMPTS_EXCEEDED);
        verify(emailVerificationStore, never()).delete(anyLong());
    }

    @Test
    void 인증번호가_다르면_불일치로_거절하고_인증번호는_남겨둔다() {
        givenValidVerification("123456");
        given(emailVerificationStore.countAttempt(1L, MAX_FAIL_COUNT)).willReturn(true);

        assertConfirmFails("654321", ErrorCode.RECRUITMENT_EMAIL_CODE_MISMATCH);
        verify(emailVerificationStore, never()).delete(anyLong());
    }

    @Test
    void 인증번호가_맞으면_인증번호를_지우고_인증_완료_토큰을_발급한다() {
        givenValidVerification("123456");
        given(emailVerificationStore.countAttempt(1L, MAX_FAIL_COUNT)).willReturn(true);
        given(jwtTokenProvider.createEmailVerificationToken(EMAIL, "SIGNUP")).willReturn("verification-token");

        String token = emailVerificationService.confirm(confirmRequest("123456")).getVerificationToken();

        assertThat(token).isEqualTo("verification-token");
        verify(emailVerificationStore).delete(1L);
    }

    private void givenValidVerification(String code) {
        LocalDateTime sentAt = LocalDateTime.now().minusMinutes(1);
        given(emailVerificationStore.find(EMAIL, PURPOSE))
                .willReturn(Optional.of(verification(code, sentAt.plus(CODE_TTL), sentAt)));
    }

    private void assertConfirmFails(String code, ErrorCode expected) {
        assertThatThrownBy(() -> emailVerificationService.confirm(confirmRequest(code)))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode").isEqualTo(expected);
    }

    private EmailVerification verification(String code, LocalDateTime expiresAt, LocalDateTime sentAt) {
        EmailVerification verification = EmailVerification.builder()
                .email(EMAIL)
                .purpose(PURPOSE)
                .code(code)
                .expiresAt(expiresAt)
                .sentAt(sentAt)
                .build();
        ReflectionTestUtils.setField(verification, "id", 1L);
        return verification;
    }

    private EmailVerificationSendRequest sendRequest(String email) {
        EmailVerificationSendRequest request = new EmailVerificationSendRequest();
        ReflectionTestUtils.setField(request, "email", email);
        ReflectionTestUtils.setField(request, "purpose", PURPOSE);
        return request;
    }

    private EmailVerificationConfirmRequest confirmRequest(String code) {
        EmailVerificationConfirmRequest request = new EmailVerificationConfirmRequest();
        ReflectionTestUtils.setField(request, "email", EMAIL);
        ReflectionTestUtils.setField(request, "purpose", PURPOSE);
        ReflectionTestUtils.setField(request, "code", code);
        return request;
    }
}

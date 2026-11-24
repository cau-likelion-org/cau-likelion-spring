package com.example.cau_likelion_spring.recruitment.service;

import com.example.cau_likelion_spring.global.exception.CustomException;
import com.example.cau_likelion_spring.global.exception.ErrorCode;
import com.example.cau_likelion_spring.global.jwt.JwtTokenProvider;
import com.example.cau_likelion_spring.global.jwt.JwtValidationType;
import com.example.cau_likelion_spring.recruitment.domain.Applicant;
import com.example.cau_likelion_spring.recruitment.domain.Round;
import com.example.cau_likelion_spring.recruitment.dto.ApplicantSignupRequest;
import com.example.cau_likelion_spring.recruitment.repository.ApplicantRepository;
import com.example.cau_likelion_spring.recruitment.repository.ApplicationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ApplicantServiceTest {

    private static final String EMAIL = "applicant@cau.ac.kr";
    private static final String TOKEN = "verification-token";

    @Mock
    private ApplicantRepository applicantRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private CurrentRoundFinder currentRoundFinder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ApplicantService applicantService;

    @Test
    void 접수_기간에_인증된_이메일로_가입하면_현재_회차의_계정을_만들고_지원자_토큰을_발급한다() {
        givenVerificationToken("SIGNUP", EMAIL);
        Round round = openRound();
        given(currentRoundFinder.find()).willReturn(round);
        given(applicantRepository.findByRoundAndEmail(round, EMAIL)).willReturn(Optional.empty());
        given(passwordEncoder.encode("password1!")).willReturn("encoded");
        given(applicantRepository.saveAndFlush(any(Applicant.class))).willAnswer(invocation -> {
            Applicant saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 7L);
            return saved;
        });
        given(jwtTokenProvider.createApplicantToken(7L)).willReturn("applicant-token");

        String accessToken = applicantService.signup(signupRequest("Applicant@CAU.ac.kr")).getAccessToken();

        ArgumentCaptor<Applicant> saved = ArgumentCaptor.forClass(Applicant.class);
        verify(applicantRepository).saveAndFlush(saved.capture());
        assertThat(saved.getValue().getRound()).isSameAs(round);
        assertThat(saved.getValue().getEmail()).isEqualTo(EMAIL);
        assertThat(saved.getValue().getPassword()).isEqualTo("encoded");
        assertThat(saved.getValue().getFormEnteredAt()).isNotNull();
        assertThat(accessToken).isEqualTo("applicant-token");
    }

    @Test
    void 인증_완료_토큰의_이메일이_요청과_다르면_거절한다() {
        givenVerificationToken("SIGNUP", "other@cau.ac.kr");

        assertSignupFails(ErrorCode.RECRUITMENT_INVALID_VERIFICATION_TOKEN);
    }

    @Test
    void 비밀번호_재설정_용도의_인증_완료_토큰으로는_가입할_수_없다() {
        givenVerificationToken("PASSWORD_RESET", EMAIL);

        assertSignupFails(ErrorCode.RECRUITMENT_INVALID_VERIFICATION_TOKEN);
    }

    @Test
    void 만료된_인증_완료_토큰이면_거절한다() {
        given(jwtTokenProvider.validateToken(TOKEN)).willReturn(JwtValidationType.EXPIRED_JWT_TOKEN);

        assertSignupFails(ErrorCode.RECRUITMENT_INVALID_VERIFICATION_TOKEN);
    }

    @Test
    void 접수_마감_후에는_가입할_수_없다() {
        givenVerificationToken("SIGNUP", EMAIL);
        LocalDateTime now = LocalDateTime.now();
        given(currentRoundFinder.find()).willReturn(
                Round.builder().startDate(now.minusDays(7)).deadLine(now.minusMinutes(1)).build());

        assertSignupFails(ErrorCode.RECRUITMENT_NOT_IN_APPLY_PERIOD);
    }

    @Test
    void 이번_회차에_미제출_계정이_있는_이메일이면_작성_중으로_거절한다() {
        givenVerificationToken("SIGNUP", EMAIL);
        Round round = openRound();
        Applicant existing = Applicant.builder().round(round).email(EMAIL).build();
        given(currentRoundFinder.find()).willReturn(round);
        given(applicantRepository.findByRoundAndEmail(round, EMAIL)).willReturn(Optional.of(existing));
        given(applicationRepository.existsByApplicantAndRoundPart_Round(existing, round)).willReturn(false);

        assertSignupFails(ErrorCode.RECRUITMENT_APPLICATION_IN_PROGRESS);
    }

    @Test
    void 이번_회차에_제출_이력이_있는_이메일이면_지원_내역_있음으로_거절한다() {
        givenVerificationToken("SIGNUP", EMAIL);
        Round round = openRound();
        Applicant existing = Applicant.builder().round(round).email(EMAIL).build();
        given(currentRoundFinder.find()).willReturn(round);
        given(applicantRepository.findByRoundAndEmail(round, EMAIL)).willReturn(Optional.of(existing));
        given(applicationRepository.existsByApplicantAndRoundPart_Round(existing, round)).willReturn(true);

        assertSignupFails(ErrorCode.RECRUITMENT_ALREADY_APPLIED);
    }

    private void givenVerificationToken(String purpose, String email) {
        given(jwtTokenProvider.validateToken(TOKEN)).willReturn(JwtValidationType.VALID_JWT);
        given(jwtTokenProvider.isEmailVerificationToken(TOKEN)).willReturn(true);
        given(jwtTokenProvider.getPurpose(TOKEN)).willReturn(purpose);
        if ("SIGNUP".equals(purpose)) {
            given(jwtTokenProvider.getEmail(TOKEN)).willReturn(email);
        }
    }

    private Round openRound() {
        LocalDateTime now = LocalDateTime.now();
        return Round.builder().startDate(now.minusDays(1)).deadLine(now.plusDays(1)).build();
    }

    private void assertSignupFails(ErrorCode expected) {
        assertThatThrownBy(() -> applicantService.signup(signupRequest(EMAIL)))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode").isEqualTo(expected);
        verify(applicantRepository, never()).saveAndFlush(any());
    }

    private ApplicantSignupRequest signupRequest(String email) {
        ApplicantSignupRequest request = new ApplicantSignupRequest();
        ReflectionTestUtils.setField(request, "name", "김멋사");
        ReflectionTestUtils.setField(request, "phone", "01012345678");
        ReflectionTestUtils.setField(request, "email", email);
        ReflectionTestUtils.setField(request, "password", "password1!");
        ReflectionTestUtils.setField(request, "privacyAgreed", true);
        ReflectionTestUtils.setField(request, "verificationToken", TOKEN);
        return request;
    }
}

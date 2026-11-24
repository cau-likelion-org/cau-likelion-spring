package com.example.cau_likelion_spring.recruitment.service;

import com.example.cau_likelion_spring.global.util.S3Uploader;
import com.example.cau_likelion_spring.recruitment.domain.Applicant;
import com.example.cau_likelion_spring.recruitment.domain.Application;
import com.example.cau_likelion_spring.recruitment.domain.Round;
import com.example.cau_likelion_spring.recruitment.domain.RoundPart;
import com.example.cau_likelion_spring.recruitment.dto.MyApplicationResponse;
import com.example.cau_likelion_spring.recruitment.repository.ApplicantInterviewTimeRepository;
import com.example.cau_likelion_spring.recruitment.repository.ApplicantRepository;
import com.example.cau_likelion_spring.recruitment.repository.ApplicationRepository;
import com.example.cau_likelion_spring.recruitment.repository.InterviewAnswerRepository;
import com.example.cau_likelion_spring.recruitment.repository.InterviewTimeRepository;
import com.example.cau_likelion_spring.recruitment.repository.RoundPartRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private RoundPartRepository roundPartRepository;

    @Mock
    private ApplicantRepository applicantRepository;

    @Mock
    private InterviewTimeRepository interviewTimeRepository;

    @Mock
    private ApplicantInterviewTimeRepository applicantInterviewTimeRepository;

    @Mock
    private InterviewAnswerRepository interviewAnswerRepository;

    @Mock
    private CurrentRoundFinder currentRoundFinder;

    @Mock
    private PartQuestionLoader partQuestionLoader;

    @Mock
    private AnswerValidator answerValidator;

    @Mock
    private S3Uploader s3Uploader;

    @InjectMocks
    private ApplicationService applicationService;

    @Test
    void 제출한_지원자는_제출_일시와_지원_파트를_받는다() {
        LocalDateTime now = LocalDateTime.now();
        Round round = Round.builder().startDate(now.minusDays(7)).deadLine(now.minusDays(1)).build();
        Applicant applicant = givenApplicant(round);
        LocalDateTime submitAt = now.minusDays(2);
        given(applicationRepository.findByApplicantAndRoundPart_Round(applicant, round))
                .willReturn(Optional.of(Application.builder()
                        .applicant(applicant)
                        .roundPart(RoundPart.builder().round(round).part("백엔드").build())
                        .submitAt(submitAt)
                        .build()));

        MyApplicationResponse response = applicationService.getMyApplication(7L);

        assertThat(response.isSubmitted()).isTrue();
        assertThat(response.getSubmitAt()).isEqualTo(submitAt);
        assertThat(response.getPart()).isEqualTo("백엔드");
        assertThat(response.isApplyOpen()).isFalse();
    }

    @Test
    void 접수_기간의_미제출_지원자는_제출_내역_없음과_접수_중을_받는다() {
        LocalDateTime now = LocalDateTime.now();
        Round round = Round.builder().startDate(now.minusDays(1)).deadLine(now.plusDays(1)).build();
        Applicant applicant = givenApplicant(round);
        given(applicationRepository.findByApplicantAndRoundPart_Round(applicant, round))
                .willReturn(Optional.empty());

        MyApplicationResponse response = applicationService.getMyApplication(7L);

        assertThat(response.isSubmitted()).isFalse();
        assertThat(response.getSubmitAt()).isNull();
        assertThat(response.getPart()).isNull();
        assertThat(response.isApplyOpen()).isTrue();
    }

    @Test
    void 마감_후의_미제출_지원자는_접수_중이_아니다() {
        LocalDateTime now = LocalDateTime.now();
        Round round = Round.builder().startDate(now.minusDays(7)).deadLine(now.minusMinutes(1)).build();
        Applicant applicant = givenApplicant(round);
        given(applicationRepository.findByApplicantAndRoundPart_Round(applicant, round))
                .willReturn(Optional.empty());

        MyApplicationResponse response = applicationService.getMyApplication(7L);

        assertThat(response.isSubmitted()).isFalse();
        assertThat(response.isApplyOpen()).isFalse();
    }

    private Applicant givenApplicant(Round round) {
        Applicant applicant = Applicant.builder().round(round).email("applicant@cau.ac.kr").build();
        given(applicantRepository.findById(7L)).willReturn(Optional.of(applicant));
        given(currentRoundFinder.find()).willReturn(round);
        return applicant;
    }
}

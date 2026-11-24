package com.example.cau_likelion_spring.recruitment.domain;

import com.example.cau_likelion_spring.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 지원자가 고른 면접 가능 시간.
 * 확정 면접 일시는 Result.fixedInterviewDate 하나만 기준으로 한다.
 */
@Entity
@Table(name = "recruitment_applicant_interview_time")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApplicantInterviewTime extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id")
    private Applicant applicant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interview_time_id")
    private InterviewTime interviewTime;

    @Builder
    public ApplicantInterviewTime(Applicant applicant, InterviewTime interviewTime) {
        this.applicant = applicant;
        this.interviewTime = interviewTime;
    }
}

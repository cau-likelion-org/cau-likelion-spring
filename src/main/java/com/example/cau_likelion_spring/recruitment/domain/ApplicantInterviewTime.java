package com.example.cau_likelion_spring.recruitment.domain;

import com.example.cau_likelion_spring.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @Enumerated(EnumType.STRING)
    private InterviewTimeStatus status;

    @Builder
    public ApplicantInterviewTime(Applicant applicant, InterviewTime interviewTime, InterviewTimeStatus status) {
        this.applicant = applicant;
        this.interviewTime = interviewTime;
        this.status = status;
    }
}

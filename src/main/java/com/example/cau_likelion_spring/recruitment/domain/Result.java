package com.example.cau_likelion_spring.recruitment.domain;

import com.example.cau_likelion_spring.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 지원 결과.
 * "result"는 의미가 너무 일반적이고 일부 DB에서 키워드로 쓰여 헷갈릴 수 있어,
 * recruitment_ 접두어를 붙인 테이블명을 명시한다.
 */
@Entity
@Table(name = "recruitment_result")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Result extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id")
    private Application application;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id")
    private Applicant applicant;

    @Enumerated(EnumType.STRING)
    private SubmitStatus submitStatus;

    @Enumerated(EnumType.STRING)
    private ApplicationResult applicationResult;

    private LocalDateTime fixedInterviewDate;

    private String fixedInterviewSpot;

    @Builder
    public Result(Application application, Applicant applicant, SubmitStatus submitStatus,
                  ApplicationResult applicationResult, LocalDateTime fixedInterviewDate,
                  String fixedInterviewSpot) {
        this.application = application;
        this.applicant = applicant;
        this.submitStatus = submitStatus;
        this.applicationResult = applicationResult;
        this.fixedInterviewDate = fixedInterviewDate;
        this.fixedInterviewSpot = fixedInterviewSpot;
    }
}

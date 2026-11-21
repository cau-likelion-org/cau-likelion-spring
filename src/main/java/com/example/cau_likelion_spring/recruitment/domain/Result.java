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
 * 결과 미입력은 null, 보류(HOLD)는 입력된 것으로 취급한다.
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

    /** 서류 결과 */
    @Enumerated(EnumType.STRING)
    private ApplicationResult documentResult;

    /** 면접(최종) 결과 */
    @Enumerated(EnumType.STRING)
    private ApplicationResult finalResult;

    private LocalDateTime fixedInterviewDate;

    private String fixedInterviewSpot;

    @Builder
    public Result(Application application, Applicant applicant, ApplicationResult documentResult,
                  ApplicationResult finalResult, LocalDateTime fixedInterviewDate,
                  String fixedInterviewSpot) {
        this.application = application;
        this.applicant = applicant;
        this.documentResult = documentResult;
        this.finalResult = finalResult;
        this.fixedInterviewDate = fixedInterviewDate;
        this.fixedInterviewSpot = fixedInterviewSpot;
    }

    /** 서류 결과 변경 (이력 미보관, 덮어쓰기) */
    public void changeDocumentResult(ApplicationResult documentResult) {
        this.documentResult = documentResult;
    }

    /** 확정 면접 일시·장소 입력 (덮어쓰기) */
    public void changeInterviewSchedule(LocalDateTime fixedInterviewDate, String fixedInterviewSpot) {
        this.fixedInterviewDate = fixedInterviewDate;
        this.fixedInterviewSpot = fixedInterviewSpot;
    }

    public boolean isDocumentPassed() {
        return documentResult == ApplicationResult.PASS;
    }
}

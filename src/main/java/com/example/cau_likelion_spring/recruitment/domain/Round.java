package com.example.cau_likelion_spring.recruitment.domain;

import com.example.cau_likelion_spring.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 모집 회차 (= 모집공고).
 * 회차와 공고가 1:1이라 별도 Notice 엔티티를 두지 않고, 공고 정보도 이 엔티티에서 관리한다.
 * "round"는 MySQL의 ROUND() 함수명과 같아 헷갈리고 의미도 일반적이라,
 * recruitment_ 접두어를 붙인 테이블명을 명시한다.
 */
@Entity
@Table(name = "recruitment_round")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Round extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    private String name;

    private LocalDateTime startDate;

    private LocalDateTime deadLine;

    private LocalDateTime interviewStartDate;

    private LocalDateTime interviewDeadLine;

    /**
     * 모집공고에 안내하는 서류 결과 발표 예정일 (모집공고 일정 API에서 입력).
     * 실제로 지원자에게 결과가 공개되는 시각은 documentResultPublishAt이다.
     */
    private LocalDateTime documentResultDate;

    /** 서류 결과 공개 예약 시각 (비어 있으면 예약 전 = 입력 중, 이 시각이 지나면 공개) */
    private LocalDateTime documentResultPublishAt;

    /** 최종 결과 공개 시각 (비어 있으면 공개 예약 전) */
    private LocalDateTime finalResultDate;

    /** 결과 확인 종료일 */
    private LocalDateTime resultCheckDeadLine;

    private LocalDateTime otDate;

    @Builder
    public Round(String name, LocalDateTime startDate, LocalDateTime deadLine,
                 LocalDateTime interviewStartDate, LocalDateTime interviewDeadLine,
                 LocalDateTime documentResultDate, LocalDateTime finalResultDate,
                 LocalDateTime resultCheckDeadLine, LocalDateTime otDate) {
        this.name = name;
        this.startDate = startDate;
        this.deadLine = deadLine;
        this.interviewStartDate = interviewStartDate;
        this.interviewDeadLine = interviewDeadLine;
        this.documentResultDate = documentResultDate;
        this.finalResultDate = finalResultDate;
        this.resultCheckDeadLine = resultCheckDeadLine;
        this.otDate = otDate;
    }

    /** 서류 접수가 시작됐는지 (시작일이 비어 있으면 아직 일정 미정 → 시작 전으로 본다) */
    public boolean isApplyStarted(LocalDateTime now) {
        return startDate != null && !now.isBefore(startDate);
    }

    /** 모집공고 일정 수정 (서류접수 시작·마감, 서류 결과발표, OT) */
    public void updateNoticeSchedule(LocalDateTime startDate, LocalDateTime deadLine,
                                     LocalDateTime documentResultDate, LocalDateTime otDate) {
        this.startDate = startDate;
        this.deadLine = deadLine;
        this.documentResultDate = documentResultDate;
        this.otDate = otDate;
    }

    public ResultPublishStatus getDocumentPublishStatus(LocalDateTime now) {
        return ResultPublishStatus.of(documentResultPublishAt, now);
    }

    /** 서류 결과 공개 예약 */
    public void reserveDocumentResult(LocalDateTime publishAt) {
        this.documentResultPublishAt = publishAt;
    }

    /** 서류 결과 공개 예약 취소 (입력 중으로 복귀) */
    public void cancelDocumentResultReservation() {
        this.documentResultPublishAt = null;
    }
}

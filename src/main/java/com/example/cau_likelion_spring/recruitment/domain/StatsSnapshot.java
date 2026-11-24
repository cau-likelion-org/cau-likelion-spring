package com.example.cau_likelion_spring.recruitment.domain;

import com.example.cau_likelion_spring.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 접수 마감 시점 통계 스냅샷 (지표 1개 = 1행).
 * 지원 데이터를 파기해도 회차별 통계를 보여주기 위해 개인을 식별할 수 없는 집계만 보관한다.
 * basisDeadLine이 현재 Round.deadLine과 다르면(마감 연장) 해당 회차 행을 지우고 다시 만든다.
 */
@Entity
@Table(name = "recruitment_stats_snapshot")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StatsSnapshot extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "round_id")
    private Round round;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private StatsMetricType metricType;

    /** 구분 값 (파트명, 학과, 날짜 등). 전체 합계 지표면 null */
    @Column(length = 100)
    private String dimensionKey;

    /**
     * 지표 값 (인원, 비율).
     * "value"는 MySQL 키워드라 컬럼명으로 그대로 쓰면 쿼리가 헷갈리므로 metric_value로 지정한다.
     */
    @Column(name = "metric_value")
    private Double value;

    /** 집계 기준 마감 시각 */
    private LocalDateTime basisDeadLine;

    @Builder
    public StatsSnapshot(Round round, StatsMetricType metricType, String dimensionKey,
                         Double value, LocalDateTime basisDeadLine) {
        this.round = round;
        this.metricType = metricType;
        this.dimensionKey = dimensionKey;
        this.value = value;
        this.basisDeadLine = basisDeadLine;
    }
}

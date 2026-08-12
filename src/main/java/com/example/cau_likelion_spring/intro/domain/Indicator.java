package com.example.cau_likelion_spring.intro.domain;

import com.example.cau_likelion_spring.global.common.BaseTimeEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 랜딩 페이지 정량 지표 (누적 기수 / 누적 수료자 / 누적 프로젝트)
 * 항상 단 하나의 row만 존재해야 한다 - IndicatorService가 조회 시점에 없으면 기본값 0으로 생성해서 이를 보장한다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Indicator extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 누적 기수
    private String cumulative_generations;

    // 누적 수료자
    private String cumulative_graduates;

    // 누적 프로젝트
    private String cumulative_projects;

    @Builder
    private Indicator(String cumulative_generations, String cumulative_graduates, String cumulative_projects) {
        this.cumulative_generations = cumulative_generations;
        this.cumulative_graduates = cumulative_graduates;
        this.cumulative_projects = cumulative_projects;
    }

    public void update(String cumulativeGenerations, String cumulativeGraduates, String cumulativeProjects) {
        this.cumulative_generations = cumulativeGenerations;
        this.cumulative_graduates = cumulativeGraduates;
        this.cumulative_projects = cumulativeProjects;
    }
}

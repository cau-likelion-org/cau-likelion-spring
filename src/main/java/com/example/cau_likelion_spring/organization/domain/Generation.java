package com.example.cau_likelion_spring.organization.domain;

import com.example.cau_likelion_spring.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 기수
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Generation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 기수 숫자 (예: 13) */
    @Column(nullable = false)
    private Integer number;

    /** 활동 년도 (예: 2026) */
    @Column(nullable = false)
    private Integer year;

    @Builder
    public Generation(Integer number, Integer year) {
        this.number = number;
        this.year = year;
    }
}

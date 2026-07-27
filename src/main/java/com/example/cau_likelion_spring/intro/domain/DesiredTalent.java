package com.example.cau_likelion_spring.intro.domain;

import com.example.cau_likelion_spring.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 인재상 (공통 인재상 / 파트별 인재상)
 * partName이 null 또는 "공통"이면 공통 인재상, 특정 파트명이 들어있으면 해당 파트의 인재상
 * (Part 엔티티를 FK로 참조하지 않고 텍스트로 저장 - ERD 기준)
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DesiredTalent extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String partName;

    @Column(length = 1000)
    private String content;

    @Builder
    public DesiredTalent(String partName, String content) {
        this.partName = partName;
        this.content = content;
    }

    public void update(String partName, String content) {
        if (partName != null) this.partName = partName;
        if (content != null) this.content = content;
    }
}

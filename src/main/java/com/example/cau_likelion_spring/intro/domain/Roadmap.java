package com.example.cau_likelion_spring.intro.domain;

import com.example.cau_likelion_spring.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 소개 페이지 연간 로드맵 이미지.
 * 단일 row 강제 정책(Indicator 등)과 달리, 새 이미지를 추가할 때마다 새 row로 쌓이고
 * 조회 시에는 가장 최근(id 내림차순 최상단) row 하나만 반환한다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Roadmap extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255, nullable = false)
    private String imageUrl;

    @Builder
    public Roadmap(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}

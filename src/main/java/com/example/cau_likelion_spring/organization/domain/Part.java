package com.example.cau_likelion_spring.organization.domain;

import com.example.cau_likelion_spring.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 파트 (예: 백엔드, 프론트엔드, 디자인 ...)
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Part extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generation_id", nullable = false)
    private Generation generation;

    /** 파트 이름 **/
    @Column(nullable = false)
    private String name;

    @Builder
    public Part(Generation generation, String name) {
        this.generation = generation;
        this.name = name;
    }
}

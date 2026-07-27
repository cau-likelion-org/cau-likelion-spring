package com.example.cau_likelion_spring.intro.domain;

import com.example.cau_likelion_spring.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 커리큘럼 - 트랙별 주차 커리큘럼
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Curriculum extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "track_id", nullable = false)
    private Track track;

    /** 주차 */
    private Integer week;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @Builder
    public Curriculum(Track track, Integer week, String title, String description) {
        this.track = track;
        this.week = week;
        this.title = title;
        this.description = description;
    }

    public void update(Track track, Integer week, String title, String description) {
        this.track = track;
        this.week = week;
        this.title = title;
        this.description = description;
    }
}

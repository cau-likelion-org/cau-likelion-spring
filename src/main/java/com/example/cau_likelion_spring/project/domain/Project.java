package com.example.cau_likelion_spring.project.domain;

import com.example.cau_likelion_spring.global.common.BaseTimeEntity;
import com.example.cau_likelion_spring.organization.domain.Generation;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Project extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generation_id", nullable = false)
    private Generation generation;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    private ProjectCategory category;

    /** 기술 스택 */
    @Lob
    private String stack;

    /** 한줄 소개 */
    @Lob
    private String tagline;

    /** 서비스 요약 - TODO 추후 글자수 확정 */
    @Lob
    private String summary;

    @Column(length = 255)
    private String teamName;

    private LocalDate startDate;

    private LocalDate endDate;

    @Column(length = 255)
    private String banner;

    private Boolean isExposed;

    @Builder
    public Project(Generation generation, String title, ProjectCategory category, String stack, String tagline,
                    String summary, String teamName, LocalDate startDate, LocalDate endDate, String banner) {
        this.generation = generation;
        this.title = title;
        this.category = category;
        this.stack = stack;
        this.tagline = tagline;
        this.summary = summary;
        this.teamName = teamName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.banner = banner;
        this.isExposed = false;
    }

    public void update(Generation generation, String title, ProjectCategory category, String stack, String tagline,
                        String summary, String teamName, LocalDate startDate, LocalDate endDate, String banner) {
        this.generation = generation;
        this.title = title;
        this.category = category;
        this.stack = stack;
        this.tagline = tagline;
        this.summary = summary;
        this.teamName = teamName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.banner = banner;
    }

    public void updateExposure(boolean isExposed) {
        this.isExposed = isExposed;
    }
}

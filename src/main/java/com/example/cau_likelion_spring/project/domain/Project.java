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
    private String summary;

    /** 상세 소개 */
    @Lob
    private String detail;

    private LocalDate startDate;

    private LocalDate endDate;

    @Builder
    public Project(Generation generation, String title, ProjectCategory category, String stack,
                    String summary, String detail, LocalDate startDate, LocalDate endDate) {
        this.generation = generation;
        this.title = title;
        this.category = category;
        this.stack = stack;
        this.summary = summary;
        this.detail = detail;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public void update(Generation generation, String title, ProjectCategory category, String stack,
                        String summary, String detail, LocalDate startDate, LocalDate endDate) {
        this.generation = generation;
        this.title = title;
        this.category = category;
        this.stack = stack;
        this.summary = summary;
        this.detail = detail;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}

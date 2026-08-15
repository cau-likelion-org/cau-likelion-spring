package com.example.cau_likelion_spring.gallery.project.domain;

import com.example.cau_likelion_spring.global.common.BaseTimeEntity;
import com.example.cau_likelion_spring.organization.domain.Generation;
import com.example.cau_likelion_spring.project.domain.ProjectCategory;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectGallery extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generation_id", nullable = false)
    private Generation generation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectCategory category;

    @Column(nullable = false)
    private String title;

    private String thumbnailUrl;

    @Lob
    private String description;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    @Builder
    public ProjectGallery(Generation generation, ProjectCategory category, String title, String thumbnailUrl,
                           String description, LocalDate startDate, LocalDate endDate) {
        this.generation = generation;
        this.category = category;
        this.title = title;
        this.thumbnailUrl = thumbnailUrl;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public void update(Generation generation, ProjectCategory category, String title, String description,
                        LocalDate startDate, LocalDate endDate) {
        this.generation = generation;
        this.category = category;
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public void updateThumbnail(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
}

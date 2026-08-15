package com.example.cau_likelion_spring.gallery.history.domain;

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
public class History extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generation_id", nullable = false)
    private Generation generation;

    @Column(nullable = false)
    private String title;

    private String thumbnailUrl;

    @Lob
    private String description;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    @Builder
    public History(Generation generation, String title, String thumbnailUrl, String description, LocalDate startDate, LocalDate endDate) {
        this.generation = generation;
        this.title = title;
        this.thumbnailUrl = thumbnailUrl;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public void update(Generation generation, String title, String description, LocalDate startDate, LocalDate endDate) {
        this.generation = generation;
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public void updateThumbnail(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
}

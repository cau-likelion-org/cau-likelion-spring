package com.example.cau_likelion_spring.gallery.domain;

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
public class Gallery extends BaseTimeEntity {

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

    private LocalDate date;

    @Builder
    public Gallery(Generation generation, String title, String thumbnailUrl, String description, LocalDate date) {
        this.generation = generation;
        this.title = title;
        this.thumbnailUrl = thumbnailUrl;
        this.description = description;
        this.date = date;
    }
}

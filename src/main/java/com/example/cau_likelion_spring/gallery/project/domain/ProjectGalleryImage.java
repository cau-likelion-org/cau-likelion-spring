package com.example.cau_likelion_spring.gallery.project.domain;

import com.example.cau_likelion_spring.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectGalleryImage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_gallery_id", nullable = false)
    private ProjectGallery projectGallery;

    private String imageUrl;

    @Builder
    public ProjectGalleryImage(ProjectGallery projectGallery, String imageUrl) {
        this.projectGallery = projectGallery;
        this.imageUrl = imageUrl;
    }
}

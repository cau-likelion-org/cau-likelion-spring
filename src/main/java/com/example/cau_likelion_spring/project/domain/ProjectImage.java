package com.example.cau_likelion_spring.project.domain;

import com.example.cau_likelion_spring.global.common.BaseTimeEntity;
import com.example.cau_likelion_spring.organization.domain.Generation;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectImage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Lob
    private String imageUrl;

    /** 썸네일 여부 */
    @Column(nullable = false)
    private Boolean isMain;

    private Integer displayOrder;

    @Builder
    public ProjectImage(Project project, Generation generation, String imageUrl, Boolean isMain, Integer displayOrder) {
        this.project = project;
        this.imageUrl = imageUrl;
        this.isMain = isMain;
        this.displayOrder = displayOrder;
    }
}

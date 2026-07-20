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
public class ProjectLink extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    private Platform platform;

    @Column(nullable = false)
    private String url;

    @Builder
    public ProjectLink(Project project, Generation generation, Platform platform, String url) {
        this.project = project;
        this.platform = platform;
        this.url = url;
    }
}

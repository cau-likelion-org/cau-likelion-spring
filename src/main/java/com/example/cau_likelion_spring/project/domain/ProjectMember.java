package com.example.cau_likelion_spring.project.domain;

import com.example.cau_likelion_spring.global.common.BaseTimeEntity;
import com.example.cau_likelion_spring.organization.domain.Generation;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 프로젝트 참여 멤버 (Member 엔티티를 FK로 참조하지 않고 이름/파트를 텍스트로 직접 저장 - ERD 기준)
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectMember extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false)
    private String name;

    /** 파트명 (varchar로 직접 저장) */
    private String part;

    @Builder
    public ProjectMember(Project project, Generation generation, String name, String part) {
        this.project = project;
        this.name = name;
        this.part = part;
    }
}

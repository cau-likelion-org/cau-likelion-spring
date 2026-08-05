package com.example.cau_likelion_spring.assignment.domain;

import com.example.cau_likelion_spring.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 과제 제출 파일 (Assignment.type = FILE인 경우 사용, 제출 하나에 여러 파일 첨부 가능)
 * 생성일(createdAt)은 BaseTimeEntity에서 상속받음
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SubmissionFile extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submit_id", nullable = false)
    private AssignmentSubmit assignmentSubmit;

    /** 스토리지 저장 경로 */
    @Column(nullable = false)
    private String fileUrl;

    @Column(nullable = false)
    private String originalFilename;

    @Builder
    public SubmissionFile(AssignmentSubmit assignmentSubmit, String fileUrl, String originalFilename) {
        this.assignmentSubmit = assignmentSubmit;
        this.fileUrl = fileUrl;
        this.originalFilename = originalFilename;
    }
}

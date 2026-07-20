package com.example.cau_likelion_spring.assignment.domain;

import com.example.cau_likelion_spring.global.common.BaseTimeEntity;
import com.example.cau_likelion_spring.member.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 과제 제출 내역
 * 제출 시각(createdAt)은 BaseTimeEntity에서 상속받음
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AssignmentSubmit extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    /** 검토 운영진 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_member_id")
    private Member reviewMember;

    /** 제출 아기사자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submit_member_id")
    private Member submitMember;

    @Lob
    private String content;

    /** 파일 업로드용 url */
    private String fileUrl;

    /** 반려 여부 */
    @Column(nullable = false)
    private Boolean isRejected = false;

    @Builder
    public AssignmentSubmit(Assignment assignment, Member reviewMember, Member submitMember, String content,
                             String fileUrl, Boolean isRejected) {
        this.assignment = assignment;
        this.reviewMember = reviewMember;
        this.submitMember = submitMember;
        this.content = content;
        this.fileUrl = fileUrl;
        this.isRejected = (isRejected != null) ? isRejected : false;
    }
}

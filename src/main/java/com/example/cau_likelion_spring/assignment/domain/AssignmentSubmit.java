package com.example.cau_likelion_spring.assignment.domain;

import com.example.cau_likelion_spring.global.common.BaseTimeEntity;
import com.example.cau_likelion_spring.member.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 과제 제출 내역
 * 제출 시각(createdAt)은 BaseTimeEntity에서 상속받음
 * 재제출마다 새 row가 생성됨 (기존 제출을 수정하지 않음)
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

    @Column(length = 300)
    private String content;

    /** Assignment.type = URL인 경우에만 사용 */
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20)")
    private AssignmentSubmitStatus status;

    private LocalDateTime approvalDate;

    @Lob
    private String rejectionReason;

    @Builder
    public AssignmentSubmit(Assignment assignment, Member submitMember, String content, String url) {
        this.assignment = assignment;
        this.submitMember = submitMember;
        this.content = content;
        this.url = url;
        this.status = AssignmentSubmitStatus.PENDING;
    }

    public void approve(Member reviewMember) {
        this.reviewMember = reviewMember;
        this.status = AssignmentSubmitStatus.APPROVED;
        this.approvalDate = LocalDateTime.now();
        this.rejectionReason = null;
    }

    public void reject(Member reviewMember, String rejectionReason) {
        this.reviewMember = reviewMember;
        this.status = AssignmentSubmitStatus.REJECTED;
        this.approvalDate = LocalDateTime.now();
        this.rejectionReason = rejectionReason;
    }
}

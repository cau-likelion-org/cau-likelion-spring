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
 * 화면에 보여줄 "제출 시각"은 BaseTimeEntity의 updatedAt을 사용한다 (아래 참고).
 * - PENDING(운영진이 아직 평가하지 않음) 상태에서 다시 제출하는 것은 '수정'이라 기존 row를 그대로 고친다.
 *   이때 createdAt(최초 제출 시각)은 유지되고 updatedAt만 갱신된다.
 * - REJECTED(반려로 평가가 이미 끝남) 이후 다시 제출하는 것은 '재제출'이라 새 row가 생긴다.
 *   반려 기록은 평가 이력이므로 덮어쓰지 않고 그대로 보존한다.
 * - APPROVED(승인으로 평가가 이미 끝남) 이후에는 더 이상 제출 자체가 불가능하다.
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

    /** 검토 운영진. 평가자가 삭제되면 이 참조는 비워지므로(reviewerName 스냅샷은 유지), 화면엔 reviewerName을 써야 한다 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_member_id")
    private Member reviewMember;

    /** 평가 시점의 검토 운영진 이름 스냅샷. reviewMember가 삭제로 비워져도 "누가 평가했는지"는 남아있어야 하므로 별도 보관 */
    @Column(length = 50)
    private String reviewerName;

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

    /** PENDING 상태에서의 '수정'. 기존 row를 그대로 고치며 status는 PENDING을 유지한다. */
    public void editSubmission(String content, String url) {
        this.content = content;
        this.url = url;
    }

    public void approve(Member reviewMember) {
        this.reviewMember = reviewMember;
        this.reviewerName = reviewMember.getName();
        this.status = AssignmentSubmitStatus.APPROVED;
        this.approvalDate = LocalDateTime.now();
        this.rejectionReason = null;
    }

    public void reject(Member reviewMember, String rejectionReason) {
        this.reviewMember = reviewMember;
        this.reviewerName = reviewMember.getName();
        this.status = AssignmentSubmitStatus.REJECTED;
        this.approvalDate = LocalDateTime.now();
        this.rejectionReason = rejectionReason;
    }
}

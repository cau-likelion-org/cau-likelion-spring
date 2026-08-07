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
 * 특정 아기사자 1명에게 적용되는 개별 마감일. 존재하면 Assignment.endDate 대신 이 값을 기준으로
 * 제출 가능 여부/정시·지각 판정을 한다 (조회 시 override로 우선 적용, DB에는 원본 마감일을 건드리지 않고 별도 저장).
 */
@Entity
@Getter
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"assignment_id", "member_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AssignmentIndividualDeadline extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private LocalDateTime deadline;

    @Builder
    public AssignmentIndividualDeadline(Assignment assignment, Member member, LocalDateTime deadline) {
        this.assignment = assignment;
        this.member = member;
        this.deadline = deadline;
    }

    public void updateDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }
}

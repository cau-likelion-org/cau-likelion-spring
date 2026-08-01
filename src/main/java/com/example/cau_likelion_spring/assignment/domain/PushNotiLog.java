package com.example.cau_likelion_spring.assignment.domain;

import com.example.cau_likelion_spring.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 과제 평가(승인/반려) 알림 발송 내역
 * 제출/수정/재제출마다 AssignmentSubmit이 새로 생기고, 그 제출 각각에 대한 평가 알림이 기록됨
 * 생성일(createdAt)은 BaseTimeEntity에서 상속받음
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PushNotiLog extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submit_id", nullable = false)
    private AssignmentSubmit assignmentSubmit;

    @Builder
    public PushNotiLog(AssignmentSubmit assignmentSubmit) {
        this.assignmentSubmit = assignmentSubmit;
    }
}

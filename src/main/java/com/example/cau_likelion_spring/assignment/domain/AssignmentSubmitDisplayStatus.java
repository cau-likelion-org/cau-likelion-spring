package com.example.cau_likelion_spring.assignment.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 화면에 보여줄 6가지 과제 제출 상태.
 * DB에 저장되지 않고, AssignmentSubmitStatus(PENDING/APPROVED/REJECTED) + 제출 이력 유무 + 마감일 대비 시각을
 * 조회 시점에 조합해서 계산한다.
 */
@Getter
@RequiredArgsConstructor
public enum AssignmentSubmitDisplayStatus {
    BEFORE_SUBMISSION("제출전"),
    MISSED("미제출"),
    PENDING_REVIEW("승인대기"),
    LATE_SUBMITTED("지각제출"),
    APPROVED("승인완료"),
    REJECTED("승인반려");

    private final String description;
}

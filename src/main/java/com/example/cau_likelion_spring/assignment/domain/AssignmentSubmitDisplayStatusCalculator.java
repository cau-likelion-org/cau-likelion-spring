package com.example.cau_likelion_spring.assignment.domain;

import java.time.LocalDateTime;

/**
 * 6가지 화면 표시 상태 계산 로직. BABY_LION/STAFF 서비스가 공통으로 사용한다.
 */
public final class AssignmentSubmitDisplayStatusCalculator {

    public static final int LATE_SUBMISSION_GRACE_DAYS = 5;

    private AssignmentSubmitDisplayStatusCalculator() {
    }

    public static AssignmentSubmitDisplayStatus calculate(Assignment assignment, AssignmentSubmit latest) {
        LocalDateTime endDate = assignment.getEndDate();

        if (latest == null) {
            boolean missed = LocalDateTime.now().isAfter(endDate.plusDays(LATE_SUBMISSION_GRACE_DAYS));
            return missed ? AssignmentSubmitDisplayStatus.MISSED : AssignmentSubmitDisplayStatus.BEFORE_SUBMISSION;
        }

        return switch (latest.getStatus()) {
            case APPROVED -> AssignmentSubmitDisplayStatus.APPROVED;
            case REJECTED -> AssignmentSubmitDisplayStatus.REJECTED;
            case PENDING -> latest.getCreatedAt().isAfter(endDate)
                    ? AssignmentSubmitDisplayStatus.LATE_SUBMITTED
                    : AssignmentSubmitDisplayStatus.PENDING_REVIEW;
        };
    }
}

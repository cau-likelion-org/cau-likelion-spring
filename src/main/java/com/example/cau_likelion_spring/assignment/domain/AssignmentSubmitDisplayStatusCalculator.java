package com.example.cau_likelion_spring.assignment.domain;

import java.time.LocalDateTime;

/**
 * 6가지 화면 표시 상태 계산 로직. BABY_LION/STAFF 서비스가 공통으로 사용한다.
 * PENDING(운영진 평가 전)은 정시/지각 여부와 상관없이 전부 '승인대기'로 보여준다.
 * 정시/지각 구분은 APPROVED(승인 완료)가 된 시점에만 드러난다 - 정시 제출은 '승인완료', 지각 제출은 '지각제출'.
 * REJECTED(반려)는 정시/지각 여부와 무관하게 '승인반려' 하나로 유지한다.
 */
public final class AssignmentSubmitDisplayStatusCalculator {

    public static final int LATE_SUBMISSION_GRACE_DAYS = 5;

    private AssignmentSubmitDisplayStatusCalculator() {
    }

    /**
     * assignment.endDate를 그대로 마감일로 쓴다. 아기사자 개별로 마감일이 연장된 경우
     * {@link #calculate(LocalDateTime, AssignmentSubmit)}에 그 개별 마감일을 넘겨서 써야 한다.
     */
    public static AssignmentSubmitDisplayStatus calculate(Assignment assignment, AssignmentSubmit latest) {
        return calculate(assignment.getEndDate(), latest);
    }

    public static AssignmentSubmitDisplayStatus calculate(LocalDateTime endDate, AssignmentSubmit latest) {
        if (latest == null) {
            boolean missed = LocalDateTime.now().isAfter(endDate.plusDays(LATE_SUBMISSION_GRACE_DAYS));
            return missed ? AssignmentSubmitDisplayStatus.MISSED : AssignmentSubmitDisplayStatus.BEFORE_SUBMISSION;
        }

        return switch (latest.getStatus()) {
            case REJECTED -> AssignmentSubmitDisplayStatus.REJECTED;
            case PENDING -> AssignmentSubmitDisplayStatus.PENDING_REVIEW;
            case APPROVED -> latest.getCreatedAt().isAfter(endDate)
                    ? AssignmentSubmitDisplayStatus.LATE_SUBMITTED
                    : AssignmentSubmitDisplayStatus.APPROVED;
        };
    }
}

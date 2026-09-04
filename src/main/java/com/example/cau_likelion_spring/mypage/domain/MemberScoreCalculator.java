package com.example.cau_likelion_spring.mypage.domain;

/**
 * 상벌점 계산 규칙을 한 곳에 모아둔 순수 계산 로직.
 * 마이페이지(본인 조회)와 운영진/회장 목록 조회가 이 클래스 하나만 공유해서 쓴다 -
 * 그래야 나중에 감점 기준이 바뀌어도 고칠 곳이 한 군데뿐이다.
 */
public final class MemberScoreCalculator {

    public static final double FULL_SCORE = 3.0;

    public static final double LATE_PENALTY = -0.5;
    public static final double ABSENT_PENALTY = -1.0;
    public static final double UNAUTHORIZED_ABSENT_PENALTY = -1.5;
    public static final double LATE_SUBMIT_PENALTY = -0.2;
    public static final double MISSED_PENALTY = -1.0;

    private MemberScoreCalculator() {
    }

    public static double calculateTotal(long lateCount, long absentCount, long unauthorizedAbsentCount,
                                         long lateSubmitCount, long missedCount) {
        double total = FULL_SCORE
                + lateCount * LATE_PENALTY
                + absentCount * ABSENT_PENALTY
                + unauthorizedAbsentCount * UNAUTHORIZED_ABSENT_PENALTY
                + lateSubmitCount * LATE_SUBMIT_PENALTY
                + missedCount * MISSED_PENALTY;

        return Math.max(total, 0);
    }

    /** 출결관리 화면(8.3.4)의 "감점" 컬럼용 - 과제 감점은 제외하고 출결로 인한 감점 크기만(양수로) 반환한다. */
    public static double calculateAttendancePenalty(long lateCount, long absentCount, long unauthorizedAbsentCount) {
        return -(lateCount * LATE_PENALTY + absentCount * ABSENT_PENALTY + unauthorizedAbsentCount * UNAUTHORIZED_ABSENT_PENALTY);
    }
}

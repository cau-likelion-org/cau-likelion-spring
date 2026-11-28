package com.example.cau_likelion_spring.recruitment.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 통계 스냅샷 지표 종류 (StatsSnapshot.metricType)
 * 대시보드(7.9.1) 화면에 나오는 값은 계산값(합계·차이·비율)까지 모두 저장한다.
 * 파트별 지원자 수·정원·경쟁률은 화면에 없으므로 저장하지 않는다.
 * 비율은 아랫값이 0이면 null로 저장한다.
 * dimensionKey가 필요한 지표는 주석에 예시를 적는다.
 * DB에는 enum 이름이 저장되고, label은 화면 표시용이다.
 */
@Getter
@RequiredArgsConstructor
public enum StatsMetricType {
    // 지원자 수·정원·경쟁률 (회차 전체)
    TOTAL_APPLICANTS("전체 지원자 수"),                 // 서류 제출자 수
    TOTAL_HEAD_COUNT("전체 모집 정원"),                 // 파트별 정원 합
    TOTAL_COMPETITION_RATE("전체 경쟁률"),             // 제출자 수 ÷ 정원 합
    CONVERSION_RATE("본인 확인 대비 제출 전환율"),     // 본인 확인 수 ÷ 제출자 수

    // 단계별 인원
    STAGE_IDENTIFIED("본인 확인 인원"),
    STAGE_SUBMITTED("서류 제출 인원"),
    STAGE_NOT_SUBMITTED("미제출 인원"),                // 본인 확인 − 제출
    LATE_SUBMITTED("지각 제출 인원"),
    STAGE_DOCUMENT_PASSED("서류 합격 인원"),
    STAGE_DOCUMENT_FAILED("서류 불합격 인원"),
    STAGE_FINAL_PASSED("면접 합격 인원"),
    STAGE_FINAL_FAILED("면접 불합격 인원"),

    // 제출 추이
    DAILY_SUBMISSIONS("일자별 제출 수"),               // 2026-11-20 (접수 시작~마감, 0명 포함)

    // 분포
    /**
     * 학과 비율(%). 학과명, 인원이 아닌 비율만 저장한다 (파트별 지원자 수가 드러나지 않게).
     * part가 null이면 전체 지원자 대비, 있으면 그 파트 지원자 대비 비율.
     */
    DEPARTMENT_RATE("학과 비율"),
    GRADE_DIST("학년 분포"),                           // Grade 값 (0명 포함)
    ENROLLMENT_DIST("학적 분포"),                      // EnrollmentStatus 값 (0명 포함)
    SOURCE_DIST("지원 경로 분포");                     // ApplicationSource 값 (0명 포함)

    private final String label;
}

package com.example.cau_likelion_spring.recruitment.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 통계 스냅샷 지표 종류 (StatsSnapshot.metricType)
 * dimensionKey가 필요한 지표는 주석에 예시를 적는다.
 * DB에는 enum 이름이 저장되고, label은 화면 표시용이다.
 */
@Getter
@RequiredArgsConstructor
public enum StatsMetricType {
    TOTAL_APPLICANTS("전체 지원자 수"),
    PART_APPLICANTS("파트별 지원자 수"),             // 파트명
    TOTAL_COMPETITION_RATE("전체 경쟁률"),
    PART_COMPETITION_RATE("파트별 경쟁률"),          // 파트명
    DAILY_SUBMISSIONS("일자별 제출 수"),             // 2026-11-20
    HOURLY_SUBMISSIONS("시간대별 제출 수"),          // 2026-11-20 14
    STAGE_IDENTIFIED("본인 확인 인원"),
    STAGE_SUBMITTED("제출 인원"),
    STAGE_DOCUMENT_PASSED("서류 합격 인원"),         // 결과 공개 후 추가
    STAGE_FINAL_PASSED("최종 합격 인원"),            // 결과 공개 후 추가
    CONVERSION_RATE("본인 확인 대비 제출 전환율"),
    DEPARTMENT_DIST("학과 분포"),                    // 학과명
    GRADE_DIST("학년 분포"),                         // Grade 값
    ENROLLMENT_DIST("학적 분포"),                    // EnrollmentStatus 값
    SOURCE_DIST("지원 경로 분포");                   // ApplicationSource 값

    private final String label;
}

package com.example.cau_likelion_spring.recruitment.domain;

/**
 * 지원자 학적, 3월 기준 (Application.enrollment)
 */
public enum EnrollmentStatus {
    ENROLLED,            // 재학
    ON_LEAVE,            // 휴학
    GRADUATION_DEFERRED  // 졸업 유예
}

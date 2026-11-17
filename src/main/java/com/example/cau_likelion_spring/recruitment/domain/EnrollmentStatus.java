package com.example.cau_likelion_spring.recruitment.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 지원자 학적, 3월 기준 (Application.enrollment)
 * DB에는 enum 이름이 저장되고, label은 화면 표시용이다.
 */
@Getter
@RequiredArgsConstructor
public enum EnrollmentStatus {
    ENROLLED("재학"),
    ON_LEAVE("휴학"),
    GRADUATION_DEFERRED("졸업 유예");

    private final String label;
}

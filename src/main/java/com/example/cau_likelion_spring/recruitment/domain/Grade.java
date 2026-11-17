package com.example.cau_likelion_spring.recruitment.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 지원자 학년, 3월 기준 (Application.grade)
 * DB에는 enum 이름이 저장되고, label은 화면 표시용이다.
 */
@Getter
@RequiredArgsConstructor
public enum Grade {
    FIRST("1학년"),
    SECOND("2학년"),
    THIRD("3학년"),
    FOURTH("4학년"),
    FIFTH_OR_ABOVE("5학년 이상");

    private final String label;
}

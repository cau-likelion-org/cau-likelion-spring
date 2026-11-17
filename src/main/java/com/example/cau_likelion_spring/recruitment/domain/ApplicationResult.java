package com.example.cau_likelion_spring.recruitment.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 지원 결과 (Result.documentResult, Result.finalResult)
 * 미입력은 null로 둔다.
 * DB에는 enum 이름이 저장되고, label은 운영진 화면 표시용이다.
 * HOLD는 지원자 화면(7.5)에서 "평가 중"으로 따로 표시한다.
 */
@Getter
@RequiredArgsConstructor
public enum ApplicationResult {
    PASS("합격"),
    FAIL("불합격"),
    HOLD("보류");

    private final String label;
}

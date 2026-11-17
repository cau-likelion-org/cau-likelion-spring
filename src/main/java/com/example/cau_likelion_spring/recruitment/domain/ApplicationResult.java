package com.example.cau_likelion_spring.recruitment.domain;

/**
 * 지원 결과 (Result.documentResult, Result.finalResult)
 * 미입력은 null로 둔다.
 */
public enum ApplicationResult {
    PASS,  // 합격
    FAIL,  // 불합격
    HOLD   // 보류 (회장단 내부용, 지원자에게는 "평가 중"으로 노출)
}

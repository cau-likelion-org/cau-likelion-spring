package com.example.cau_likelion_spring.recruitment.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 서류 결과 공개 예약 조건 (최종 결과 조건은 최종 결과 공개 예약 기능과 함께 추가) */
@Getter
@RequiredArgsConstructor
public enum ResultPublishCondition {
    DOCUMENT_RESULT_ENTERED("전원 서류 결과 입력 (보류 포함)"),
    INTERVIEW_SCHEDULE_ENTERED("서류 합격자 전원 면접 일시 입력"),
    DOCUMENT_TEMPLATES_WRITTEN("서류 결과 발표 문구 2종 작성 (합격·불합격)");

    private final String label;
}

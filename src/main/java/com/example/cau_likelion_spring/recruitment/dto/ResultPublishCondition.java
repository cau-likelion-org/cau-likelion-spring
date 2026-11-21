package com.example.cau_likelion_spring.recruitment.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 결과 공개 예약 조건 */
@Getter
@RequiredArgsConstructor
public enum ResultPublishCondition {
    // 서류 결과
    DOCUMENT_RESULT_ENTERED("전원 서류 결과 입력 (보류 포함)"),
    INTERVIEW_SCHEDULE_ENTERED("서류 합격자 전원 면접 일시·장소 입력"),
    DOCUMENT_TEMPLATES_WRITTEN("서류 결과 발표 문구 2종 작성 (합격·불합격)"),
    // 최종 결과
    DOCUMENT_PUBLISH_RESERVED("서류 결과 공개 예약 완료"),
    FINAL_RESULT_ENTERED("서류 합격자 전원 면접 결과 입력 (보류 포함)"),
    FINAL_TEMPLATES_WRITTEN("최종 결과 발표 문구 2종 작성 (합격·불합격)");

    private final String label;
}

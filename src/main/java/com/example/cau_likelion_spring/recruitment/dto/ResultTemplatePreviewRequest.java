package com.example.cau_likelion_spring.recruitment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** 백오피스 결과 안내 문구 미리보기 요청 (저장 전 편집 중인 문구도 미리볼 수 있도록 문구를 함께 보낸다) */
@Schema(description = "결과 안내 문구 미리보기 요청")
public record ResultTemplatePreviewRequest(

        @Schema(description = "미리볼 문구 (편집 중인 내용)",
                example = "{이름}님, 서류 전형에 합격하셨습니다.\n면접 일시: {면접 일시}")
        @NotBlank @Size(max = 2000) String text,

        @Schema(description = "미리보기 대상 지원서 id (현재 회차에 제출된 지원서)", example = "1")
        @NotNull Long applicationId
) {
}

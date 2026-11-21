package com.example.cau_likelion_spring.recruitment.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** 백오피스 결과 안내 문구 미리보기 응답 */
@Schema(description = "결과 안내 문구 미리보기")
public record ResultTemplatePreviewResponse(

        @Schema(description = "지원서 id", example = "1")
        Long applicationId,

        @Schema(description = "변수를 실제 값으로 바꾼 문구. 면접 일시·장소가 아직 입력되지 않았으면 '미정'으로 표시됩니다.",
                example = "홍길동님, 서류 전형에 합격하셨습니다.\n면접 일시: 2027년 3월 14일 (일) 15:30")
        String message
) {
}

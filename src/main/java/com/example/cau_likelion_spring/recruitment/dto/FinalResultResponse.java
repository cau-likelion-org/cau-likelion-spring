package com.example.cau_likelion_spring.recruitment.dto;

import com.example.cau_likelion_spring.recruitment.domain.ApplicationResult;
import com.example.cau_likelion_spring.recruitment.domain.Result;
import io.swagger.v3.oas.annotations.media.Schema;

/** 백오피스 면접(최종) 결과 입력 응답 (지원서별 저장된 결과) */
@Schema(description = "지원서별 면접 결과")
public record FinalResultResponse(

        @Schema(description = "지원서 id", example = "1")
        Long applicationId,

        @Schema(description = "저장된 면접 결과", example = "PASS")
        ApplicationResult finalResult
) {
    public static FinalResultResponse from(Result result) {
        return new FinalResultResponse(result.getApplication().getId(), result.getFinalResult());
    }
}

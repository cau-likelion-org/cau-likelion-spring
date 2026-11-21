package com.example.cau_likelion_spring.recruitment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/** 백오피스 2단계 문항 순서 변경 요청 - 파트의 전체 문항 id를 바뀐 순서대로 보낸다 */
@Schema(description = "백오피스 2단계 문항 순서 변경 요청")
public record QuestionOrderRequest(

        @Schema(description = "바뀐 순서대로 나열한 파트의 전체 문항 id (중복·누락 불가)", example = "[3, 1, 2]")
        @NotEmpty
        List<@NotNull Long> questionIds
) {
}

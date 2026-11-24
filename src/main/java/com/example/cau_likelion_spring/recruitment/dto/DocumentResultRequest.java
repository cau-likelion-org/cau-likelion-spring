package com.example.cau_likelion_spring.recruitment.dto;

import com.example.cau_likelion_spring.recruitment.domain.ApplicationResult;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 백오피스 서류 결과 입력 요청.
 * 체크박스 일괄 변경은 여러 id, 행별 개별 변경은 id 하나를 보낸다.
 */
@Schema(description = "백오피스 서류 결과 입력 요청")
public record DocumentResultRequest(

        @Schema(description = "결과를 입력할 지원서 id 목록 (개별 변경은 1개)", example = "[1, 2, 3]")
        @NotEmpty
        List<@NotNull Long> applicationIds,

        @Schema(description = "서류 결과 (PASS: 합격, FAIL: 불합격, HOLD: 보류 - 회장단 내부 표시용)", example = "PASS")
        @NotNull
        ApplicationResult result
) {
}

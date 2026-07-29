package com.example.cau_likelion_spring.assignment.dto;

import com.example.cau_likelion_spring.assignment.domain.AssignmentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Schema(description = "과제 생성 요청")
public record AssignmentRequest(

        @Schema(description = "주차", example = "1")
        @NotNull Integer week,

        @Schema(description = "과제명")
        @NotBlank String title,

        @Schema(description = "과제 설명")
        String detail,

        @Schema(description = "마감 기한")
        @NotNull LocalDateTime endDate,

        @Schema(description = "제출 형식")
        @NotNull AssignmentType type
) {
}

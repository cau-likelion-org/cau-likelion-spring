package com.example.cau_likelion_spring.assignment.dto;

import com.example.cau_likelion_spring.assignment.domain.AssignmentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "과제 생성 요청 (한 주차에 개별 과제 1개 이상을 한 번에 생성)")
public record AssignmentCreateRequest(

        @Schema(description = "주차", example = "1")
        @NotNull Integer week,

        @Schema(description = "개별 과제 목록 (1개 이상)")
        @NotEmpty @Valid List<AssignmentItem> assignments
) {

    @Schema(description = "개별 과제")
    public record AssignmentItem(

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
}

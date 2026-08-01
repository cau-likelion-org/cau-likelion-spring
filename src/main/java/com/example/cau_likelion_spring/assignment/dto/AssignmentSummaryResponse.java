package com.example.cau_likelion_spring.assignment.dto;

import com.example.cau_likelion_spring.assignment.domain.AssignmentSubmitDisplayStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "주차 내 개별 과제 요약 (아기사자 본인 기준)")
public record AssignmentSummaryResponse(

        @Schema(description = "과제 ID", example = "1")
        Long assignmentId,

        @Schema(description = "과제명")
        String title,

        @Schema(description = "마감 기한")
        LocalDateTime endDate,

        @Schema(description = "화면 표시용 제출 상태")
        AssignmentSubmitDisplayStatus status,

        @Schema(description = "제출 시각 (제출 이력이 없으면 null)")
        LocalDateTime submittedAt
) {
}

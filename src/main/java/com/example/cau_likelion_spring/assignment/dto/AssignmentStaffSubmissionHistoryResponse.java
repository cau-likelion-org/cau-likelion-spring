package com.example.cau_likelion_spring.assignment.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "개별 과제의 파트원 전체 제출 이력 (운영진 본인 파트 기준, 재제출 이력 모두 포함)")
public record AssignmentStaffSubmissionHistoryResponse(

        @Schema(description = "과제 ID", example = "1")
        Long assignmentId,

        @Schema(description = "과제명")
        String title,

        @Schema(description = "과제 설명")
        String detail,

        @Schema(description = "마감 기한")
        LocalDateTime endDate,

        @Schema(description = "파트원별 제출 이력 목록")
        List<AssignmentMemberSubmissionHistoryResponse> submissions
) {
}

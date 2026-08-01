package com.example.cau_likelion_spring.assignment.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "주차 내 개별 과제 요약 및 파트원 제출 현황 집계 (운영진 본인 파트 기준)")
public record AssignmentStaffSummaryResponse(

        @Schema(description = "과제 ID", example = "1")
        Long assignmentId,

        @Schema(description = "과제명")
        String title,

        @Schema(description = "마감 기한")
        LocalDateTime endDate,

        @Schema(description = "제출전 인원 수 (마감 전, 한 번도 제출한 적 없음)")
        int beforeSubmissionCount,

        @Schema(description = "미제출 인원 수 (마감+유예기간이 지나도록 한 번도 제출한 적 없음)")
        int missedCount,

        @Schema(description = "승인대기 인원 수 (마감 전 제출, 아직 평가 전)")
        int pendingReviewCount,

        @Schema(description = "지각제출 인원 수 (마감 후 제출, 아직 평가 전)")
        int lateSubmittedCount,

        @Schema(description = "승인완료 인원 수")
        int approvedCount
) {
}

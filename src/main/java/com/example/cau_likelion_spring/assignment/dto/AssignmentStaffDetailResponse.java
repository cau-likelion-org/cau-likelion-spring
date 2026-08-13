package com.example.cau_likelion_spring.assignment.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "개별 과제의 파트원 전체 제출 현황 (운영진 본인 파트 기준)")
public record AssignmentStaffDetailResponse(

        @Schema(description = "과제 ID", example = "1")
        Long assignmentId,

        @Schema(description = "과제명")
        String title,

        @Schema(description = "과제 설명")
        String detail,

        @Schema(description = "마감 기한")
        LocalDateTime endDate,

        @Schema(description = "파트원 전체의 제출 현황 목록 (아기사자 이름/최종 제출 시각/제출물/상태/리뷰 운영진 이름/평가에 쓸 submitId)")
        List<AssignmentMemberSubmissionResponse> submissions
) {

    @Schema(name = "AssignmentStaffDetailWeekGroup", description = "주차별로 묶인 과제별 파트원 제출 현황 (운영진 본인 파트 기준)")
    public record WeekGroup(

            @Schema(description = "주차", example = "1")
            Integer week,

            @Schema(description = "해당 주차의 개별 과제별 제출 현황 목록")
            List<AssignmentStaffDetailResponse> assignments
    ) {
    }
}

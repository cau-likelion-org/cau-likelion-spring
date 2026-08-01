package com.example.cau_likelion_spring.assignment.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "주차별로 묶인 과제 목록 (아기사자 본인 기준)")
public record AssignmentWeekGroupResponse(

        @Schema(description = "주차", example = "1")
        Integer week,

        @Schema(description = "해당 주차의 개별 과제 목록")
        List<AssignmentSummaryResponse> assignments
) {
}

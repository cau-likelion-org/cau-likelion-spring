package com.example.cau_likelion_spring.assignment.dto;

import com.example.cau_likelion_spring.assignment.domain.AssignmentSubmitDisplayStatus;
import com.example.cau_likelion_spring.assignment.domain.AssignmentType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "주차 내 개별 과제의 제출 이력 전체 (아기사자 본인 기준)")
public record AssignmentSubmissionHistoryResponse(

        @Schema(description = "과제 ID", example = "1")
        Long assignmentId,

        @Schema(description = "과제명")
        String title,

        @Schema(description = "과제 설명")
        String detail,

        @Schema(description = "마감 기한 (개별 마감일이 있으면 그 값, 없으면 과제 공통 마감일)")
        LocalDateTime endDate,

        @Schema(description = "제출 형식 (URL 링크 / 파일 업로드)")
        AssignmentType type,

        @Schema(description = "본인의 제출 이력 전체 (최신순, 제출 이력이 없으면 빈 배열)")
        List<AssignmentSubmitResponse> submissions
) {

    @Schema(name = "AssignmentSubmissionHistoryWeekGroup", description = "주차별로 묶인 과제별 제출 이력 (아기사자 본인 기준)")
    public record WeekGroup(

            @Schema(description = "주차", example = "1")
            Integer week,

            @Schema(description = "이 주차 종합 상태. 개별 과제 상태들을 우선순위(제출전 > 승인반려 > 승인대기 > 미제출 > 지각제출 > 승인완료)로 판단해 하나로 합친 값")
            AssignmentSubmitDisplayStatus weeklyStatus,

            @Schema(description = "해당 주차의 개별 과제별 제출 이력 목록")
            List<AssignmentSubmissionHistoryResponse> assignments
    ) {
    }
}

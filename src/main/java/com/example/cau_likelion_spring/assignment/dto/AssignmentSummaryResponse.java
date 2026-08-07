package com.example.cau_likelion_spring.assignment.dto;

import com.example.cau_likelion_spring.assignment.domain.AssignmentSubmitDisplayStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

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

    @Schema(description = "주차별로 묶인 과제 목록 (아기사자 본인 기준)")
    public record WeekGroup(

            @Schema(description = "주차", example = "1")
            Integer week,

            @Schema(description = "이 주차 종합 상태. 개별 과제 상태들을 우선순위(제출전 > 승인반려 > 승인대기 > 미제출 > 지각제출 > 승인완료)로 판단해 하나로 합친 값")
            AssignmentSubmitDisplayStatus weeklyStatus,

            @Schema(description = "해당 주차의 개별 과제 목록")
            List<AssignmentSummaryResponse> assignments
    ) {
    }
}

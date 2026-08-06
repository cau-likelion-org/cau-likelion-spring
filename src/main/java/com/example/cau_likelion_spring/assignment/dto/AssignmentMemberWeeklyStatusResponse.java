package com.example.cau_likelion_spring.assignment.dto;

import com.example.cau_likelion_spring.assignment.domain.AssignmentSubmitDisplayStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "아기사자 1명의 특정 주차 종합 상태. 해당 주차 개별 과제들의 상태를 우선순위(제출전 > 승인반려 > 승인대기 > 미제출 > 지각제출 > 승인완료)로 판단해 하나로 합친 값")
public record AssignmentMemberWeeklyStatusResponse(

        @Schema(description = "멤버 ID", example = "1")
        Long memberId,

        @Schema(description = "멤버 이름")
        String memberName,

        @Schema(description = "주차", example = "1")
        Integer week,

        @Schema(description = "주차 종합 상태")
        AssignmentSubmitDisplayStatus status
) {
}

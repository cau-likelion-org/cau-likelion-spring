package com.example.cau_likelion_spring.assignment.dto;

import com.example.cau_likelion_spring.assignment.domain.AssignmentSubmitStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "과제 제출 평가(승인/반려) 요청")
public record AssignmentSubmitEvaluateRequest(

        @Schema(description = "평가 상태 (APPROVED 또는 REJECTED만 허용)", example = "APPROVED")
        @NotNull
        AssignmentSubmitStatus status,

        @Schema(description = "반려 사유 (status가 REJECTED일 때 필수)")
        String rejectionReason
) {
}

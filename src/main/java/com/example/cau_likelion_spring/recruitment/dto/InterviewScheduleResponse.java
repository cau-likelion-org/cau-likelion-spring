package com.example.cau_likelion_spring.recruitment.dto;

import com.example.cau_likelion_spring.recruitment.domain.Result;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/** 백오피스 확정 면접 일정 입력 응답 */
@Schema(description = "지원서별 확정 면접 일정")
public record InterviewScheduleResponse(

        @Schema(description = "지원서 id", example = "1")
        Long applicationId,

        @Schema(description = "확정 면접 일시", example = "2027-03-14T15:30:00")
        LocalDateTime fixedInterviewDate,

        @Schema(description = "면접 장소 (미입력이면 null)", example = "310관 728호")
        String fixedInterviewSpot
) {
    public static InterviewScheduleResponse from(Result result) {
        return new InterviewScheduleResponse(result.getApplication().getId(),
                result.getFixedInterviewDate(), result.getFixedInterviewSpot());
    }
}

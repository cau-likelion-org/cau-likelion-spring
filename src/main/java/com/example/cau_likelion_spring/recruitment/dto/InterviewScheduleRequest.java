package com.example.cau_likelion_spring.recruitment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * 백오피스 확정 면접 일정 입력 요청 (지원자 한 명).
 * 지원자가 고른 면접 가능 시간 밖이거나 같은 일시 중복 배정이어도 저장되고, 응답 warnings로 알린다.
 */
@Schema(description = "백오피스 확정 면접 일정 입력 요청")
public record InterviewScheduleRequest(

        @Schema(description = "확정 면접 일시", example = "2027-03-14T15:30:00")
        @NotNull LocalDateTime fixedInterviewDate,

        @Schema(description = "면접 장소 (선택)", example = "310관 728호")
        @Size(max = 255) String fixedInterviewSpot
) {
}

package com.example.cau_likelion_spring.attendance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

@Schema(description = "출석부 생성 요청")
public record WeeklyAttendanceCreateRequest(

        @Schema(description = "세션 날짜", example = "2026-07-28")
        @NotNull LocalDate date,

        @Schema(description = "출석 체크용 비밀번호 (4자리 숫자)", example = "0091")
        @Pattern(regexp = "\\d{4}", message = "비밀번호는 4자리 숫자여야 합니다.")
        String password,

        @Schema(description = "주차", example = "1")
        @NotNull @Positive Integer weekNumber
) {
}

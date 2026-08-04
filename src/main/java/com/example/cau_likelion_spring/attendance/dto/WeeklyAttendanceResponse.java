package com.example.cau_likelion_spring.attendance.dto;

import com.example.cau_likelion_spring.attendance.domain.WeeklyAttendance;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "출석부 생성 응답")
public record WeeklyAttendanceResponse(

        @Schema(description = "출석부(주차) ID")
        Long id,

        @Schema(description = "주차")
        Integer weekNumber,

        @Schema(description = "세션 날짜")
        LocalDate date,

        @Schema(description = "출석 체크용 비밀번호")
        String password
) {

    public static WeeklyAttendanceResponse from(WeeklyAttendance weeklyAttendance) {
        return new WeeklyAttendanceResponse(
                weeklyAttendance.getId(),
                weeklyAttendance.getWeekNumber(),
                weeklyAttendance.getDate(),
                weeklyAttendance.getPassword()
        );
    }
}

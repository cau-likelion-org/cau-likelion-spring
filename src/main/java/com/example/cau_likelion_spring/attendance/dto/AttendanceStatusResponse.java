package com.example.cau_likelion_spring.attendance.dto;

import com.example.cau_likelion_spring.attendance.domain.AttendanceStatus;
import com.example.cau_likelion_spring.attendance.domain.DetailAttendance;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AttendanceStatusResponse(

        @Schema(description = "출석부(주차) ID")
        Long weeklyAttendanceId,

        @Schema(description = "주차")
        Integer weekNumber,

        @Schema(description = "세션 날짜")
        LocalDate date,

        @Schema(description = "출결 상태")
        AttendanceStatus status,

        @Schema(description = "출결 상태 설명", example = "지각")
        String statusDescription,

        @Schema(description = "출석 완료 시점 (정상 출석/지각인 경우에만 기록됨)")
        LocalDateTime checkedAt,

        @Schema(description = "결석/공결 사유 (운영진이 작성)")
        String reason
) {

    public static AttendanceStatusResponse from(DetailAttendance detailAttendance) {
        return new AttendanceStatusResponse(
                detailAttendance.getWeeklyAttendance().getId(),
                detailAttendance.getWeeklyAttendance().getWeekNumber(),
                detailAttendance.getWeeklyAttendance().getDate(),
                detailAttendance.getStatus(),
                detailAttendance.getStatus().getDescription(),
                detailAttendance.getCheckedAt(),
                detailAttendance.getDetailReason()
        );
    }
}

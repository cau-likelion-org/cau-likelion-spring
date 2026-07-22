package com.example.cau_likelion_spring.attendance.controller;

import com.example.cau_likelion_spring.attendance.dto.AttendanceStatusResponse;
import com.example.cau_likelion_spring.attendance.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Attendance", description = "출결 현황 API")
@RestController
@RequestMapping("/api/attendances")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @Operation(summary = "본인 출결 현황 조회", description = "로그인한 아기사자 본인의 주차별 출결 현황을 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<List<AttendanceStatusResponse>> getMyAttendances(@AuthenticationPrincipal Long memberId) {
        return ResponseEntity.ok(attendanceService.getMyAttendances(memberId));
    }
}

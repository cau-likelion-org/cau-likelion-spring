package com.example.cau_likelion_spring.attendance.controller;

import com.example.cau_likelion_spring.attendance.dto.AttendanceCheckRequest;
import com.example.cau_likelion_spring.attendance.dto.AttendanceStatusBatchUpdateRequest;
import com.example.cau_likelion_spring.attendance.dto.AttendanceStatusResponse;
import com.example.cau_likelion_spring.attendance.dto.MemberAttendanceResponse;
import com.example.cau_likelion_spring.attendance.dto.WeeklyAttendanceCreateRequest;
import com.example.cau_likelion_spring.attendance.dto.WeeklyAttendanceResponse;
import com.example.cau_likelion_spring.attendance.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Attendance", description = "출결 현황 API")
@RestController
@RequestMapping("/api/attendances")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @Operation(summary = "출석부 생성", description = "회장이 세션 당일 날짜, 비밀번호, 주차를 입력해 출석부를 생성합니다. " +
            "생성과 동시에 전체 아기사자에게 '출석 전' 상태가 자동 부여됩니다. PRESIDENT 권한이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "요청값 검증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "409", description = "해당 날짜에 이미 출석부가 존재함")
    })
    @PostMapping("/password")
    public ResponseEntity<WeeklyAttendanceResponse> createWeeklyAttendance(
            @Valid @RequestBody WeeklyAttendanceCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(attendanceService.createWeeklyAttendance(request));
    }

    @Operation(summary = "출석체크", description = "아기사자가 세션 당일 마이페이지에서 비밀번호 4자리를 입력해 출석 체크합니다. " +
            "세션 시작(19시) 5분 후까지는 출석, 이후 22시까지는 지각으로 처리됩니다. BABY_LION 권한이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "체크 성공"),
            @ApiResponse(responseCode = "400", description = "비밀번호 불일치 또는 체크 가능 시간 아님"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "금일 출석부가 아직 생성되지 않았거나, 출석 대상이 아님")
    })
    @PostMapping("/check")
    public ResponseEntity<AttendanceStatusResponse> checkAttendance(
            @AuthenticationPrincipal Long memberId, @Valid @RequestBody AttendanceCheckRequest request) {
        return ResponseEntity.ok(attendanceService.checkAttendance(memberId, request));
    }

    @Operation(summary = "본인 출결 현황 조회", description = "로그인한 아기사자 본인의 주차별 출결 현황을 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<List<AttendanceStatusResponse>> getMyAttendances(@AuthenticationPrincipal Long memberId) {
        return ResponseEntity.ok(attendanceService.getMyAttendances(memberId));
    }

    @Operation(summary = "본인 파트 아기사자 출결 현황 조회", description = "로그인한 운영진이 본인 파트 아기사자들의 주차별 출결 현황을 조회합니다. "
            + "각 아기사자의 출결로 인한 감점(attendancePenalty)도 함께 반환합니다 (과제 감점은 제외).")
    @GetMapping("/part")
    public ResponseEntity<List<MemberAttendanceResponse>> getPartAttendances(@AuthenticationPrincipal Long memberId) {
        return ResponseEntity.ok(attendanceService.getPartAttendances(memberId));
    }

    @Operation(summary = "출결 상태 일괄 수정", description = "운영진/회장이 여러 아기사자의 출결 상태를 한 번에 수정합니다. " +
            "결석 또는 공결로 변경할 때는 사유가 필수입니다. STAFF는 본인 파트 아기사자만, PRESIDENT는 전체 파트를 수정할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "요청값 검증 실패 또는 결석/공결 사유 누락"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (본인 파트 아님)"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 출결 기록")
    })
    @PatchMapping("/batch")
    public ResponseEntity<List<AttendanceStatusResponse>> updateAttendanceStatuses(
            @AuthenticationPrincipal Long memberId, @Valid @RequestBody AttendanceStatusBatchUpdateRequest request) {
        return ResponseEntity.ok(attendanceService.updateAttendanceStatuses(memberId, request));
    }
}

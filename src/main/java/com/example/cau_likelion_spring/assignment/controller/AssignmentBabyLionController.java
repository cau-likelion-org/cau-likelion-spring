package com.example.cau_likelion_spring.assignment.controller;

import com.example.cau_likelion_spring.assignment.dto.AssignmentWeekGroupResponse;
import com.example.cau_likelion_spring.assignment.service.AssignmentBabyLionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Assignment", description = "과제 API (아기사자)")
@RestController
@RequestMapping("/api/assignments/me")
@RequiredArgsConstructor
public class AssignmentBabyLionController {

    private final AssignmentBabyLionService assignmentBabyLionService;

    @Operation(summary = "내 과제 목록 조회 (주차별)",
            description = "로그인한 아기사자 본인 파트의 과제 목록을 주차별로 묶어서 조회합니다. "
                    + "week 파라미터를 주면 해당 주차만, 주지 않으면 전체 주차를 반환합니다. "
                    + "개별 과제마다 과제명/마감기한/제출 상태/제출 시각을 포함합니다. "
                    + "BABY_LION 권한이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "409", description = "아기사자에게 배정된 파트가 없음")
    })
    @GetMapping
    public ResponseEntity<List<AssignmentWeekGroupResponse>> getMyAssignments(
            @AuthenticationPrincipal Long memberId,
            @Parameter(description = "조회할 주차 (미지정 시 전체 주차)") @RequestParam(required = false) Integer week) {
        return ResponseEntity.ok(assignmentBabyLionService.getMyAssignments(memberId, week));
    }
}

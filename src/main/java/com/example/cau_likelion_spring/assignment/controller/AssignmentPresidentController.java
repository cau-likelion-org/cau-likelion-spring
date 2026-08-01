package com.example.cau_likelion_spring.assignment.controller;

import com.example.cau_likelion_spring.assignment.dto.AssignmentStaffWeekGroupResponse;
import com.example.cau_likelion_spring.assignment.service.AssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Assignment(PRESIDENT)", description = "과제 API (회장)")
@RestController
@RequestMapping("/api/assignments/president")
@RequiredArgsConstructor
public class AssignmentPresidentController {

    private final AssignmentService assignmentService;

    @Operation(summary = "파트별 생성된 과제 목록 조회 (주차별)",
            description = "로그인한 회장이 partId로 지정한 파트의 과제 목록을 주차별로 묶어서 조회합니다. "
                    + "본인 소속 파트로 제한되는 STAFF용 API와 달리 모든 파트를 조회할 수 있습니다. "
                    + "개별 과제마다 과제명/마감기한과, 파트원 전체를 대상으로 최신 제출 기준 제출전/미제출/승인대기/지각제출/승인완료 인원 수를 함께 보여줍니다. "
                    + "PRESIDENT 권한이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 파트")
    })
    @GetMapping
    public ResponseEntity<List<AssignmentStaffWeekGroupResponse>> getAssignmentsForPresident(
            @Parameter(description = "조회할 파트 ID", required = true) @RequestParam Long partId) {
        return ResponseEntity.ok(assignmentService.getAssignmentsForPresident(partId));
    }
}

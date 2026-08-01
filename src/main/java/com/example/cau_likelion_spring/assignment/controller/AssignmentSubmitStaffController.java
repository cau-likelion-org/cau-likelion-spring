package com.example.cau_likelion_spring.assignment.controller;

import com.example.cau_likelion_spring.assignment.dto.AssignmentMemberSubmissionResponse;
import com.example.cau_likelion_spring.assignment.service.AssignmentSubmitStaffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Assignment", description = "과제 API")
@RestController
@RequestMapping("/api/assignments/{assignmentId}/submissions/staff")
@RequiredArgsConstructor
public class AssignmentSubmitStaffController {

    private final AssignmentSubmitStaffService assignmentSubmitStaffService;

    @Operation(summary = "파트원 전체 제출 현황 조회",
            description = "로그인한 운영진이 본인 파트원 전체의 과제 제출 현황을 조회합니다. 제출한 파트원은 최종 제출본만 노출되고, "
                    + "한 번도 제출하지 않은 파트원도 제출전/미제출 상태로 함께 포함됩니다. STAFF 권한이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음 또는 본인 파트의 과제가 아님"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 과제"),
            @ApiResponse(responseCode = "409", description = "운영진에게 배정된 파트가 없음")
    })
    @GetMapping
    public ResponseEntity<List<AssignmentMemberSubmissionResponse>> getSubmissionsForStaff(
            @AuthenticationPrincipal Long memberId,
            @Parameter(description = "과제 ID", required = true) @PathVariable Long assignmentId) {
        return ResponseEntity.ok(assignmentSubmitStaffService.getSubmissionsForStaff(memberId, assignmentId));
    }
}

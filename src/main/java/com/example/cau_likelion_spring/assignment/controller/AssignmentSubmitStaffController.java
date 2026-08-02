package com.example.cau_likelion_spring.assignment.controller;

import com.example.cau_likelion_spring.assignment.dto.AssignmentMemberSubmissionResponse;
import com.example.cau_likelion_spring.assignment.dto.AssignmentSubmitEvaluateRequest;
import com.example.cau_likelion_spring.assignment.dto.AssignmentSubmitResponse;
import com.example.cau_likelion_spring.assignment.service.AssignmentSubmitStaffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Assignment (STAFF)", description = "과제 관련 API (운영진)")
@RestController
@RequestMapping("/api/assignments/{assignmentId}/submissions/staff")
@RequiredArgsConstructor
public class AssignmentSubmitStaffController {

    private final AssignmentSubmitStaffService assignmentSubmitStaffService;

    @Operation(summary = "파트원 전체 제출 현황 조회",
            description = "로그인한 운영진이 본인 파트원 전체의 과제 제출 현황을 조회합니다. 제출한 파트원은 최종 제출본(제출물/최종 제출 시각/상태)만 노출되고, "
                    + "한 번도 제출하지 않은 파트원도 제출전/미제출 상태로 함께 포함됩니다. 평가(승인/반려)가 완료된 제출이면 평가한 운영진 이름(reviewerName)도 함께 내려줍니다. "
                    + "STAFF 권한이 필요합니다.")
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

    @Operation(summary = "과제 제출 평가 (승인/반려)",
            description = "로그인한 운영진이 특정 제출을 승인 또는 반려로 평가합니다. status를 APPROVED로 보내면 "
                    + "제출 시각이 마감일 이전이면 '승인완료', 마감일 이후면 '지각제출'로 표시되고, "
                    + "status를 REJECTED로 보내면 제출 시각과 무관하게 '승인반려'로 표시됩니다 (이때 rejectionReason 필수). "
                    + "평가하면 평가한 운영진(reviewerName)과 평가 일시(approvalDate)가 함께 기록됩니다. STAFF 권한이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "평가 성공"),
            @ApiResponse(responseCode = "400", description = "요청값 검증 실패 (REJECTED인데 반려 사유 누락 등)"),
            @ApiResponse(responseCode = "403", description = "권한 없음 또는 본인 파트의 과제가 아님"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 과제 또는 제출"),
            @ApiResponse(responseCode = "409", description = "운영진에게 배정된 파트가 없음")
    })
    @PatchMapping("/{submitId}")
    public ResponseEntity<AssignmentSubmitResponse> evaluate(
            @AuthenticationPrincipal Long memberId,
            @Parameter(description = "과제 ID", required = true) @PathVariable Long assignmentId,
            @Parameter(description = "제출 ID", required = true) @PathVariable Long submitId,
            @Valid @RequestBody AssignmentSubmitEvaluateRequest request) {
        return ResponseEntity.ok(assignmentSubmitStaffService.evaluate(memberId, assignmentId, submitId, request));
    }
}

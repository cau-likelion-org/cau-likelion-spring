package com.example.cau_likelion_spring.assignment.controller;

import com.example.cau_likelion_spring.assignment.dto.AssignmentCreateRequest;
import com.example.cau_likelion_spring.assignment.dto.AssignmentIndividualDeadlineRequest;
import com.example.cau_likelion_spring.assignment.dto.AssignmentIndividualDeadlineResponse;
import com.example.cau_likelion_spring.assignment.dto.AssignmentMemberSubmissionResponse;
import com.example.cau_likelion_spring.assignment.dto.AssignmentMemberWeeklyStatusResponse;
import com.example.cau_likelion_spring.assignment.dto.AssignmentResponse;
import com.example.cau_likelion_spring.assignment.dto.AssignmentStaffDetailResponse;
import com.example.cau_likelion_spring.assignment.dto.AssignmentStaffSummaryResponse;
import com.example.cau_likelion_spring.assignment.dto.AssignmentSubmitEvaluateRequest;
import com.example.cau_likelion_spring.assignment.dto.AssignmentSubmitResponse;
import com.example.cau_likelion_spring.assignment.dto.AssignmentUpdateRequest;
import com.example.cau_likelion_spring.assignment.service.AssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Assignment (STAFF)", description = "과제 관련 API (운영진)")
@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;

    // 과제 목록/현황 조회
    @Operation(summary = "내 파트 생성된 과제 목록 조회 (주차별)",
            description = "로그인한 운영진이 본인 파트에 생성한 과제 목록을 주차별로 묶어서 조회합니다. "
                    + "개별 과제마다 과제명/마감기한과, 파트원 전체를 대상으로 최신 제출 기준 제출전/미제출/승인대기/지각제출/승인완료 인원 수를 함께 보여줍니다. "
                    + "STAFF 권한이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "409", description = "운영진에게 배정된 파트가 없음")
    })
    @GetMapping("/staff")
    public ResponseEntity<List<AssignmentStaffSummaryResponse.WeekGroup>> getMyAssignmentsForStaff(
            @AuthenticationPrincipal Long memberId) {
        return ResponseEntity.ok(assignmentService.getMyAssignmentsForStaff(memberId));
    }

    @Operation(summary = "(회장용) 파트별 생성된 과제 목록 조회 (주차별)",
            description = "로그인한 회장이 partId로 지정한 파트의 과제 목록을 주차별로 묶어서 조회합니다. "
                    + "본인 소속 파트로 제한되는 위 API와 달리 모든 파트를 조회할 수 있습니다. "
                    + "개별 과제마다 과제명/마감기한과, 파트원 전체를 대상으로 최신 제출 기준 제출전/미제출/승인대기/지각제출/승인완료 인원 수를 함께 보여줍니다. "
                    + "PRESIDENT 권한이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 파트")
    })
    @GetMapping("/president")
    public ResponseEntity<List<AssignmentStaffSummaryResponse.WeekGroup>> getAssignmentsForPresident(
            @Parameter(description = "조회할 파트 ID", required = true) @RequestParam Long partId) {
        return ResponseEntity.ok(assignmentService.getAssignmentsForPresident(partId));
    }

    @Operation(summary = "(개발용) 아기사자 1명의 특정 주차 종합 상태 조회",
            description = "로그인한 운영진이 본인 파트 아기사자 1명을 memberId로 지정해, 특정 주차(week)의 종합 상태 하나를 조회합니다. "
                    + "한 주차에 과제가 여러 개면 그 개별 과제 상태들을 우선순위(제출전 > 승인반려 > 승인대기 > 미제출 > 지각제출 > 승인완료)로 판단해 하나로 합칩니다. "
                    + "STAFF 권한이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음 또는 본인 파트 소속이 아닌 아기사자"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 멤버 또는 해당 주차에 생성된 과제가 없음"),
            @ApiResponse(responseCode = "409", description = "운영진에게 배정된 파트가 없음")
    })
    @GetMapping("/staff/weekly-status")
    public ResponseEntity<AssignmentMemberWeeklyStatusResponse> getWeeklyStatusForStaff(
            @AuthenticationPrincipal Long memberId,
            @Parameter(description = "조회할 아기사자 멤버 ID", required = true) @RequestParam Long targetMemberId,
            @Parameter(description = "조회할 주차", required = true) @RequestParam Integer week) {
        return ResponseEntity.ok(assignmentService.getWeeklyStatusForStaff(memberId, targetMemberId, week));
    }

    @Operation(summary = "내 파트 아기사자 과제 현황 목록 조회 (주차별)",
            description = "로그인한 운영진이 본인 파트의 과제 제출 현황을 주차 → 개별 과제 → 아기사자별 내역 순으로 조회합니다. "
                    + "아기사자별 내역에는 이름/최종 제출 시각/제출물(링크·파일+코멘트)/상태/평가한 운영진 이름이 포함되고, "
                    + "평가 버튼(승인/반려) 호출에 필요한 제출 ID(submitId)도 함께 내려줍니다. STAFF 권한이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "409", description = "운영진에게 배정된 파트가 없음")
    })
    @GetMapping("/staff/submissions")
    public ResponseEntity<List<AssignmentStaffDetailResponse.WeekGroup>> getSubmissionStatusForStaff(
            @AuthenticationPrincipal Long memberId) {
        return ResponseEntity.ok(assignmentService.getSubmissionStatusForStaff(memberId));
    }

    // 과제 생성/수정/삭제/개별 마감일 관리
    @Operation(summary = "과제 생성",
            description = "로그인한 운영진이 본인 파트의 과제를 생성합니다. 파트는 요청으로 받지 않고 로그인한 운영진의 소속 파트로 자동 지정됩니다. "
                    + "한 주차에 개별 과제를 1개 이상 한 번에 생성할 수 있습니다 (생성 페이지의 + 버튼으로 여러 개 모아 한 번에 저장). "
                    + "STAFF 권한이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "요청값 검증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "409", description = "운영진에게 배정된 파트가 없음")
    })
    @PostMapping
    public ResponseEntity<List<AssignmentResponse>> create(
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody AssignmentCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(assignmentService.create(memberId, request));
    }

    @Operation(summary = "과제 수정",
            description = "로그인한 운영진이 본인 파트의 과제를 수정합니다. 파트/주차는 수정할 수 없고 과제명/설명/마감기한/제출형식만 변경됩니다. "
                    + "수정 전까지 제출된 과제 제출 이력은 그대로 유지됩니다. STAFF 권한이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "요청값 검증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음 또는 본인 파트의 과제가 아님"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 과제"),
            @ApiResponse(responseCode = "409", description = "운영진에게 배정된 파트가 없음")
    })
    @PutMapping("/{id}")
    public ResponseEntity<AssignmentResponse> update(
            @AuthenticationPrincipal Long memberId,
            @Parameter(description = "과제 ID", required = true) @PathVariable Long id,
            @Valid @RequestBody AssignmentUpdateRequest request) {
        return ResponseEntity.ok(assignmentService.update(memberId, id, request));
    }

    @Operation(summary = "개별 마감일 변경",
            description = "로그인한 운영진이 선택한 아기사자(들)에게 이 과제의 개별 마감일을 부여/변경합니다 (다중 선택 가능, 선택된 전원에게 동일한 마감일 적용). "
                    + "개별 마감일이 있는 아기사자는 이후 제출 가능 여부와 정시/지각 판정 모두 과제 공통 마감일 대신 이 개별 마감일 기준으로 계산됩니다. "
                    + "이미 개별 마감일이 있는 아기사자면 값을 덮어씁니다. STAFF 권한이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "변경 성공"),
            @ApiResponse(responseCode = "400", description = "요청값 검증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음, 본인 파트의 과제가 아님, 또는 본인 파트 소속이 아닌 아기사자가 포함됨"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 과제 또는 존재하지 않는 멤버가 포함됨"),
            @ApiResponse(responseCode = "409", description = "운영진에게 배정된 파트가 없음")
    })
    @PatchMapping("/{id}/deadline")
    public ResponseEntity<List<AssignmentIndividualDeadlineResponse>> updateIndividualDeadlines(
            @AuthenticationPrincipal Long memberId,
            @Parameter(description = "과제 ID", required = true) @PathVariable Long id,
            @Valid @RequestBody AssignmentIndividualDeadlineRequest request) {
        return ResponseEntity.ok(assignmentService.updateIndividualDeadlines(memberId, id, request));
    }

    @Operation(summary = "과제 삭제",
            description = "로그인한 운영진이 본인 파트의 과제를 삭제합니다. 이전에 제출된 과제 제출 이력도 함께 삭제됩니다. STAFF 권한이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음 또는 본인 파트의 과제가 아님"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 과제"),
            @ApiResponse(responseCode = "409", description = "운영진에게 배정된 파트가 없음")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal Long memberId,
            @Parameter(description = "과제 ID", required = true) @PathVariable Long id) {
        assignmentService.delete(memberId, id);
        return ResponseEntity.noContent().build();
    }

    // 파트원 제출 현황 조회 및 평가
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
    @GetMapping("/{assignmentId}/submissions/staff")
    public ResponseEntity<List<AssignmentMemberSubmissionResponse>> getSubmissionsForStaff(
            @AuthenticationPrincipal Long memberId,
            @Parameter(description = "과제 ID", required = true) @PathVariable Long assignmentId) {
        return ResponseEntity.ok(assignmentService.getSubmissionsForStaff(memberId, assignmentId));
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
    @PatchMapping("/{assignmentId}/submissions/staff/{submitId}")
    public ResponseEntity<AssignmentSubmitResponse> evaluate(
            @AuthenticationPrincipal Long memberId,
            @Parameter(description = "과제 ID", required = true) @PathVariable Long assignmentId,
            @Parameter(description = "제출 ID", required = true) @PathVariable Long submitId,
            @Valid @RequestBody AssignmentSubmitEvaluateRequest request) {
        return ResponseEntity.ok(assignmentService.evaluate(memberId, assignmentId, submitId, request));
    }
}

package com.example.cau_likelion_spring.assignment.controller;

import com.example.cau_likelion_spring.assignment.dto.AssignmentSubmitRequest;
import com.example.cau_likelion_spring.assignment.dto.AssignmentSubmitResponse;
import com.example.cau_likelion_spring.assignment.service.AssignmentSubmitService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Assignment", description = "과제 API")
@RestController
@RequestMapping("/api/assignments/{assignmentId}/submissions")
@RequiredArgsConstructor
public class AssignmentSubmitController {

    private final AssignmentSubmitService assignmentSubmitService;

    @Operation(summary = "과제 제출/재제출",
            description = "로그인한 아기사자 본인이 과제를 제출합니다. 재제출도 이 API를 그대로 호출하며, 매번 새 제출 이력이 생성됩니다. "
                    + "Assignment.type이 URL이면 url을, FILE이면 files를 채워야 합니다. "
                    + "마감 전에는 자유롭게 (재)제출 가능하고, 마감 후에는 한 번도 제출한 적 없다면 마감일로부터 5일 이내까지만, "
                    + "이미 제출한 적 있다면 최근 제출이 반려(REJECTED)된 경우에만 기간 제한 없이 재제출할 수 있습니다. "
                    + "BABY_LION 권한이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "제출 성공"),
            @ApiResponse(responseCode = "400", description = "요청값 검증 실패 (제출 형식 불일치 등)"),
            @ApiResponse(responseCode = "403", description = "권한 없음 또는 본인 파트의 과제가 아님"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 과제"),
            @ApiResponse(responseCode = "409", description = "제출 가능 기한이 지남")
    })
    @PostMapping
    public ResponseEntity<AssignmentSubmitResponse> submit(
            @AuthenticationPrincipal Long memberId,
            @Parameter(description = "과제 ID", required = true) @PathVariable Long assignmentId,
            @Valid @RequestBody AssignmentSubmitRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(assignmentSubmitService.submit(memberId, assignmentId, request));
    }

    @Operation(summary = "본인 제출 현황 조회",
            description = "로그인한 아기사자 본인의 가장 최근 제출 내역을 조회합니다. 제출 이력이 없으면 204를 반환합니다. BABY_LION 권한이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "204", description = "제출 이력 없음"),
            @ApiResponse(responseCode = "403", description = "권한 없음 또는 본인 파트의 과제가 아님"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 과제")
    })
    @GetMapping("/me")
    public ResponseEntity<AssignmentSubmitResponse> getMySubmission(
            @AuthenticationPrincipal Long memberId,
            @Parameter(description = "과제 ID", required = true) @PathVariable Long assignmentId) {
        AssignmentSubmitResponse response = assignmentSubmitService.getMySubmission(memberId, assignmentId);
        if (response == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "본인 제출 이력 전체 조회",
            description = "로그인한 아기사자 본인이 재제출한 것을 포함한 모든 제출 이력을 최신순으로 조회합니다. BABY_LION 권한이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음 또는 본인 파트의 과제가 아님"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 과제")
    })
    @GetMapping("/me/history")
    public ResponseEntity<List<AssignmentSubmitResponse>> getMySubmissionHistory(
            @AuthenticationPrincipal Long memberId,
            @Parameter(description = "과제 ID", required = true) @PathVariable Long assignmentId) {
        return ResponseEntity.ok(assignmentSubmitService.getMySubmissionHistory(memberId, assignmentId));
    }
}

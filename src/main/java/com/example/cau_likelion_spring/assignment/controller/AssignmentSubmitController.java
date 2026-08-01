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

@Tag(name = "Assignment", description = "과제 API (아기사자)")
@RestController
@RequestMapping("/api/assignments/{assignmentId}/submissions")
@RequiredArgsConstructor
public class AssignmentSubmitController {

    private final AssignmentSubmitService assignmentSubmitService;

    @Operation(summary = "과제 제출/수정/재제출",
            description = "로그인한 아기사자 본인이 과제를 제출합니다. 최초 제출 이후에도 이 API를 그대로 호출합니다. "
                    + "제출 시각(submittedAt)은 항상 가장 최근 호출 시각으로 갱신됩니다. Assignment.type이 URL이면 url을, FILE이면 files를 채워야 합니다. "
                    + "마감 전 재호출은 '수정'으로 취급되어, 아직 운영진이 평가하지 않은(PENDING) 기존 제출을 그대로 고칩니다 (새 이력이 쌓이지 않음). "
                    + "마감 후에는 한 번도 제출한 적 없다면 마감일로부터 5일 이내까지만 '제출'할 수 있고, "
                    + "이미 제출한 적 있다면 최근 제출이 반려(REJECTED)된 경우에만 '재제출'로 기간 제한 없이 다시 제출할 수 있으며 이 경우엔 새 이력이 생성됩니다 (반려 기록 보존). "
                    + "승인 대기(PENDING) 상태라도 마감이 지났다면 더 이상 수정/재제출할 수 없습니다. "
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

    @Operation(summary = "본인 제출 이력 전체 조회",
            description = "로그인한 아기사자 본인의 제출 이력을 최신순으로 조회합니다. 마감 전 '수정'은 기존 이력을 그대로 고치므로 새 이력으로 쌓이지 않고, "
                    + "반려 후 '재제출'만 새 이력으로 남습니다 (운영진이 실제로 평가한/평가할 제출만 이력에 남음). BABY_LION 권한이 필요합니다.")
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

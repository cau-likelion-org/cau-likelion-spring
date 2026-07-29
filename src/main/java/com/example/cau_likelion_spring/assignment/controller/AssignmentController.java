package com.example.cau_likelion_spring.assignment.controller;

import com.example.cau_likelion_spring.assignment.dto.AssignmentRequest;
import com.example.cau_likelion_spring.assignment.dto.AssignmentResponse;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Assignment", description = "과제 API")
@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;

    @Operation(summary = "과제 생성",
            description = "로그인한 운영진이 본인 파트의 과제를 생성합니다. 파트는 요청으로 받지 않고 로그인한 운영진의 소속 파트로 자동 지정됩니다. "
                    + "STAFF 권한이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "요청값 검증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "409", description = "운영진에게 배정된 파트가 없음")
    })
    @PostMapping
    public ResponseEntity<AssignmentResponse> create(
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody AssignmentRequest request) {
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
}

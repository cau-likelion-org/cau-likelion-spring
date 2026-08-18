package com.example.cau_likelion_spring.member.controller;

import com.example.cau_likelion_spring.member.dto.AllowedUserEmailResponse;
import com.example.cau_likelion_spring.member.dto.AllowedUserEmailSyncRequest;
import com.example.cau_likelion_spring.member.service.AllowedUserEmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "AllowedUserEmail (Admin)", description = "예비 회원(사전 등록 이메일) 관리 API")
@RestController
@RequestMapping("/api/allowed-emails")
@RequiredArgsConstructor
public class AllowedUserEmailController {

    private final AllowedUserEmailService allowedUserEmailService;

    @Operation(summary = "예비 회원 목록 조회", description = "특정 기수에 등록된 예비 회원(아직 가입 안 한) 목록을 조회합니다.")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRESIDENT')")
    @GetMapping
    public ResponseEntity<List<AllowedUserEmailResponse>> getList(
            @Parameter(description = "기수 id", required = true) @RequestParam Long generationId) {
        return ResponseEntity.ok(allowedUserEmailService.getList(generationId));
    }

    @Operation(summary = "예비 회원 일괄 저장", description = "관리자 화면의 \"저장\" 클릭 시, 그 시점에 화면에 보이는 목록 전체를 그대로 전달합니다. "
            + "id가 없으면 신규 생성, 있으면 수정, 기존에 있었지만 이번 요청에 없는 항목은 삭제됩니다.")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRESIDENT')")
    @PutMapping
    public ResponseEntity<List<AllowedUserEmailResponse>> sync(
            @Parameter(description = "기수 id", required = true) @RequestParam Long generationId,
            @Valid @RequestBody AllowedUserEmailSyncRequest request) {
        return ResponseEntity.ok(allowedUserEmailService.sync(generationId, request));
    }
}

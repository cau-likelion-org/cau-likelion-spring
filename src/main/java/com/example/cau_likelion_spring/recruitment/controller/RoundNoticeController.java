package com.example.cau_likelion_spring.recruitment.controller;

import com.example.cau_likelion_spring.recruitment.dto.RoundNoticeRequest;
import com.example.cau_likelion_spring.recruitment.dto.RoundNoticeResponse;
import com.example.cau_likelion_spring.recruitment.service.RoundNoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Recruitment Notice", description = "리크루팅 모집공고(회차 일정 + FAQ) API")
@RestController
@RequestMapping("/api/recruitment/rounds")
@RequiredArgsConstructor
public class RoundNoticeController {

    private final RoundNoticeService roundNoticeService;

    @Operation(summary = "모집공고 등록",
            description = "일정(서류접수 시작·마감, 서류 결과발표, OT)과 FAQ 목록을 한 번에 저장해 새 모집 회차를 만듭니다. "
                    + "새로 만든 회차가 현재 회차가 됩니다. 일정은 시작 < 마감 < 결과발표 < OT 순서여야 하고, "
                    + "FAQ는 최소 1개 있어야 합니다(아니면 400, 아무것도 저장되지 않음). FAQ 배열 순서가 노출 순서입니다. "
                    + "ADMIN/PRESIDENT 권한이 필요합니다.")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRESIDENT')")
    @PostMapping
    public ResponseEntity<RoundNoticeResponse> createNotice(@Valid @RequestBody RoundNoticeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roundNoticeService.createNotice(request));
    }

    @Operation(summary = "현재 모집공고 조회",
            description = "현재 회차(가장 최근에 만든 회차)의 모집공고 일정과 FAQ(노출 순서대로)를 반환합니다. "
                    + "로그인 없이 조회할 수 있습니다. 회차가 없으면 404입니다.")
    @GetMapping("/current")
    public ResponseEntity<RoundNoticeResponse> getCurrentNotice() {
        return ResponseEntity.ok(roundNoticeService.getCurrentNotice());
    }

    @Operation(summary = "모집공고 수정",
            description = "일정과 FAQ 목록을 한 번에 덮어씁니다 (부분 수정 아님). FAQ는 보낸 목록으로 통째로 교체되며 id가 새로 발급됩니다. "
                    + "검증은 등록과 같고, 실패하면 아무것도 저장되지 않습니다. ADMIN/PRESIDENT 권한이 필요합니다.")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRESIDENT')")
    @PutMapping("/{roundId}")
    public ResponseEntity<RoundNoticeResponse> updateNotice(
            @Parameter(description = "모집 회차 id") @PathVariable Long roundId,
            @Valid @RequestBody RoundNoticeRequest request) {
        return ResponseEntity.ok(roundNoticeService.updateNotice(roundId, request));
    }
}

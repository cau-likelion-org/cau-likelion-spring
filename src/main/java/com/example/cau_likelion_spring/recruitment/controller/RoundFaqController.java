package com.example.cau_likelion_spring.recruitment.controller;

import com.example.cau_likelion_spring.recruitment.dto.RoundFaqRequest;
import com.example.cau_likelion_spring.recruitment.dto.RoundFaqResponse;
import com.example.cau_likelion_spring.recruitment.service.RoundFaqService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Recruitment Notice FAQ", description = "리크루팅 모집공고 FAQ API")
@RestController
@RequestMapping("/api/recruitment/rounds")
@RequiredArgsConstructor
public class RoundFaqController {

    private final RoundFaqService roundFaqService;

    @Operation(summary = "현재 모집공고 FAQ 조회",
            description = "현재 회차의 FAQ를 노출 순서(displayOrder)대로 반환합니다. 로그인 없이 조회할 수 있습니다. 회차가 없으면 404입니다.")
    @GetMapping("/current/faqs")
    public ResponseEntity<List<RoundFaqResponse>> getCurrentFaqs() {
        return ResponseEntity.ok(roundFaqService.getCurrentFaqs());
    }

    @Operation(summary = "모집공고 FAQ 저장",
            description = "회차의 FAQ 목록을 요청한 목록으로 통째로 바꿉니다(추가·삭제·순서 변경을 한 번에). "
                    + "배열 순서가 곧 노출 순서입니다. FAQ는 최소 1개 있어야 하며, 빈 배열이면 400입니다. "
                    + "저장할 때마다 FAQ id가 새로 발급됩니다. ADMIN/PRESIDENT 권한이 필요합니다.")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRESIDENT')")
    @PutMapping("/{roundId}/faqs")
    public ResponseEntity<List<RoundFaqResponse>> replaceFaqs(
            @Parameter(description = "모집 회차 id") @PathVariable Long roundId,
            @Valid @RequestBody RoundFaqRequest request) {
        return ResponseEntity.ok(roundFaqService.replaceFaqs(roundId, request));
    }
}

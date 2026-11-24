package com.example.cau_likelion_spring.intro.controller;

import com.example.cau_likelion_spring.intro.dto.FaqOrderRequestDto;
import com.example.cau_likelion_spring.intro.dto.FaqRequestDto;
import com.example.cau_likelion_spring.intro.dto.FaqResponseDto;
import com.example.cau_likelion_spring.intro.service.FaqService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "FAQ (Admin)", description = "랜딩페이지 FAQ 생성/조회/수정/삭제 API")
@RestController
@RequestMapping("/api/admin/faqs")
@RequiredArgsConstructor
public class FaqController {

    private final FaqService faqService;

    @Operation(summary = "FAQ 생성", description = "ADMIN 권한이 필요합니다.")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRESIDENT')")
    @PostMapping
    public ResponseEntity<FaqResponseDto> createFaq(@Valid @RequestBody FaqRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(faqService.createFaq(request));
    }

    @Operation(summary = "FAQ 리스트 조회", description = "노출 순서(displayOrder) 오름차순으로 반환합니다.")
    @GetMapping
    public ResponseEntity<List<FaqResponseDto>> getFaqList() {
        return ResponseEntity.ok(faqService.getFaqList());
    }

    @Operation(summary = "FAQ 수정", description = "요청 바디로 전체 필드를 덮어씁니다 (부분 수정 아님). answer는 최대 1000자입니다. ADMIN 권한이 필요합니다.")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRESIDENT')")
    @PutMapping("/{id}")
    public ResponseEntity<FaqResponseDto> updateFaq(
            @Parameter(description = "FAQ id") @PathVariable Long id,
            @Valid @RequestBody FaqRequestDto request) {
        return ResponseEntity.ok(faqService.updateFaq(id, request));
    }

    @Operation(summary = "FAQ 순서 변경",
            description = "바뀐 순서대로 전체 FAQ id를 보내면 앞에서부터 displayOrder를 1, 2, 3...으로 저장합니다. "
                    + "전체 FAQ id가 중복 없이 모두 포함되어야 합니다(아니면 400). 변경된 순서의 FAQ 목록을 반환합니다. ADMIN 권한이 필요합니다.")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRESIDENT')")
    @PatchMapping("/order")
    public ResponseEntity<List<FaqResponseDto>> changeFaqOrder(@Valid @RequestBody FaqOrderRequestDto request) {
        return ResponseEntity.ok(faqService.changeFaqOrder(request));
    }

    @Operation(summary = "FAQ 삭제", description = "ADMIN 권한이 필요합니다.")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRESIDENT')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFaq(@Parameter(description = "FAQ id") @PathVariable Long id) {
        faqService.deleteFaq(id);
        return ResponseEntity.noContent().build();
    }
}

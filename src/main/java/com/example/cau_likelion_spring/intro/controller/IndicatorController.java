package com.example.cau_likelion_spring.intro.controller;

import com.example.cau_likelion_spring.intro.dto.IndicatorRequestDto;
import com.example.cau_likelion_spring.intro.dto.IndicatorResponseDto;
import com.example.cau_likelion_spring.intro.service.IndicatorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "정량 지표 (Admin)", description = "랜딩페이지 누적 기수/수료자/프로젝트 지표 조회/수정 API")
@RestController
@RequestMapping("/api/admin/indicator")
@RequiredArgsConstructor
public class IndicatorController {

    private final IndicatorService indicatorService;

    @Operation(summary = "정량 지표 조회", description = "엔티티가 없으면 기본값 0으로 새로 생성해서 반환합니다.")
    @GetMapping
    public ResponseEntity<IndicatorResponseDto> getIndicator() {
        return ResponseEntity.ok(indicatorService.getIndicator());
    }

    @Operation(summary = "정량 지표 수정", description = "요청 바디로 전체 필드를 덮어씁니다. ADMIN 권한이 필요합니다.")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRESIDENT')")
    @PutMapping
    public ResponseEntity<IndicatorResponseDto> updateIndicator(
            @Valid @RequestBody IndicatorRequestDto request) {
        return ResponseEntity.ok(indicatorService.updateIndicator(request));
    }
}

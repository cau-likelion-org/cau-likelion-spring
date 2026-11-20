package com.example.cau_likelion_spring.recruitment.controller;

import com.example.cau_likelion_spring.recruitment.dto.RoundPartHeadCountRequest;
import com.example.cau_likelion_spring.recruitment.dto.RoundPartHeadCountResponse;
import com.example.cau_likelion_spring.recruitment.service.RoundPartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Recruitment Round Part (Admin)", description = "리크루팅 모집 파트·정원 관리 API")
@RestController
@RequestMapping("/api/recruitment/round-parts")
@RequiredArgsConstructor
public class RoundPartController {

    private final RoundPartService roundPartService;

    @Operation(summary = "모집 기수의 파트·정원 조회",
            description = "입력한 기수 번호의 파트 목록을 불러오고, 현재 회차에 등록된 파트별 정원을 함께 반환합니다. "
                    + "정원을 아직 등록하지 않은 파트는 roundPartId·headCount가 null입니다. ADMIN/PRESIDENT 권한이 필요합니다.")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRESIDENT')")
    @GetMapping
    public ResponseEntity<RoundPartHeadCountResponse> getHeadCounts(
            @Parameter(description = "모집 기수 번호 (예: 15)") @RequestParam Integer generation) {
        return ResponseEntity.ok(roundPartService.getHeadCounts(generation));
    }

    @Operation(summary = "모집 기수의 파트별 정원 등록·수정",
            description = "현재 회차에 파트별 정원을 등록합니다. 이미 등록된 파트는 정원만 수정됩니다. "
                    + "partId는 조회 API의 parts.partId이며, 해당 기수의 파트여야 합니다. ADMIN/PRESIDENT 권한이 필요합니다.")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRESIDENT')")
    @PutMapping
    public ResponseEntity<RoundPartHeadCountResponse> upsertHeadCounts(
            @Parameter(description = "모집 기수 번호 (예: 15)") @RequestParam Integer generation,
            @Valid @RequestBody RoundPartHeadCountRequest request) {
        return ResponseEntity.ok(roundPartService.upsertHeadCounts(generation, request));
    }
}

package com.example.cau_likelion_spring.recruitment.controller;

import com.example.cau_likelion_spring.recruitment.dto.RoundPartHeadCountRequest;
import com.example.cau_likelion_spring.recruitment.dto.RoundPartHeadCountResponse;
import com.example.cau_likelion_spring.recruitment.service.RoundPartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Recruitment Round Part (Admin)", description = "리크루팅 모집 파트·정원 관리 API")
@RestController
@RequestMapping("/api/recruitment/round-parts")
@RequiredArgsConstructor
public class RoundPartController {

    private final RoundPartService roundPartService;

    @Operation(summary = "현재 회차의 파트·정원 조회",
            description = "현재 회차에 연결된 기수(기수 생성 시 자동 연결)의 파트 목록과 등록된 파트별 정원을 함께 반환합니다. "
                    + "정원을 아직 등록하지 않은 파트는 roundPartId·headCount가 null입니다. "
                    + "회차가 없거나 회차에 기수가 연결되지 않았으면 404입니다. ADMIN/PRESIDENT 권한이 필요합니다.")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRESIDENT')")
    @GetMapping
    public ResponseEntity<RoundPartHeadCountResponse> getHeadCounts() {
        return ResponseEntity.ok(roundPartService.getHeadCounts());
    }

    @Operation(summary = "현재 회차의 파트별 정원 등록",
            description = "현재 회차에 파트별 정원을 처음 등록합니다. partId는 조회 API의 parts.partId이며, 현재 회차 기수의 파트여야 합니다. "
                    + "요청한 파트 중 이미 등록된 파트가 있으면 409를 반환합니다(수정은 PATCH). ADMIN/PRESIDENT 권한이 필요합니다.")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRESIDENT')")
    @PostMapping
    public ResponseEntity<RoundPartHeadCountResponse> createHeadCounts(
            @Valid @RequestBody RoundPartHeadCountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roundPartService.createHeadCounts(request));
    }

    @Operation(summary = "현재 회차의 파트별 정원 수정",
            description = "현재 회차에 이미 등록된 파트의 정원을 수정합니다. "
                    + "요청한 파트 중 등록되지 않은 파트가 있으면 404를 반환합니다(등록은 POST). ADMIN/PRESIDENT 권한이 필요합니다.")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRESIDENT')")
    @PatchMapping
    public ResponseEntity<RoundPartHeadCountResponse> updateHeadCounts(
            @Valid @RequestBody RoundPartHeadCountRequest request) {
        return ResponseEntity.ok(roundPartService.updateHeadCounts(request));
    }
}

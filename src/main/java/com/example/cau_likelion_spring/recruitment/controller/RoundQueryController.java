package com.example.cau_likelion_spring.recruitment.controller;

import com.example.cau_likelion_spring.recruitment.dto.RoundSummaryResponse;
import com.example.cau_likelion_spring.recruitment.service.RoundQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Recruitment Round", description = "리크루팅 회차 조회 API")
@RestController
@RequestMapping("/api/recruitment/rounds")
@RequiredArgsConstructor
public class RoundQueryController {

    private final RoundQueryService roundQueryService;

    @Operation(summary = "회차 목록 조회",
            description = "전체 모집 회차를 최근에 만든 회차 먼저 반환합니다 (대시보드 회차 선택용). "
                    + "회차가 없으면 빈 배열입니다. ADMIN/PRESIDENT 권한이 필요합니다.")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRESIDENT')")
    @GetMapping
    public ResponseEntity<List<RoundSummaryResponse>> getRounds() {
        return ResponseEntity.ok(roundQueryService.getRounds());
    }
}

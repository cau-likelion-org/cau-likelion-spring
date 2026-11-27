package com.example.cau_likelion_spring.recruitment.controller;

import com.example.cau_likelion_spring.recruitment.dto.DashboardResponse;
import com.example.cau_likelion_spring.recruitment.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Recruitment Dashboard", description = "리크루팅 대시보드 API")
@RestController
@RequestMapping("/api/recruitment/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "리크루팅 대시보드 조회",
            description = "회차의 지원자 수·경쟁률·전환율, 단계별 인원, 날짜별 제출 추이, 학과 비율, 학년·학적·지원 경로 분포를 반환합니다. "
                    + "roundId가 없으면 현재 회차입니다. 결과 확인 종료 전에는 실시간 집계(snapshot=false), "
                    + "종료 후에는 회차당 한 번 저장된 스냅샷(snapshot=true)을 반환합니다. "
                    + "비율은 소수 둘째 자리 반올림이며 아랫값이 0이면 null입니다. ADMIN/PRESIDENT 권한이 필요합니다.")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRESIDENT')")
    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard(
            @Parameter(description = "회차 id (회차 목록 조회의 id, 없으면 현재 회차)")
            @RequestParam(required = false) Long roundId) {
        return ResponseEntity.ok(dashboardService.getDashboard(roundId));
    }
}

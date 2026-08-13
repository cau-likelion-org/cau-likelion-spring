package com.example.cau_likelion_spring.intro.controller;

import com.example.cau_likelion_spring.intro.dto.RoadmapRequestDto;
import com.example.cau_likelion_spring.intro.dto.RoadmapResponseDto;
import com.example.cau_likelion_spring.intro.service.RoadmapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "로드맵 (Admin)", description = "소개 페이지 연간 로드맵 이미지 추가/조회 API")
@RestController
@RequestMapping("/api/admin/roadmap")
@RequiredArgsConstructor
public class RoadmapController {

    private final RoadmapService roadmapService;

    @Operation(summary = "로드맵 이미지 추가", description = "새 로드맵 이미지를 추가합니다. 기존 이미지를 덮어쓰지 않고 새 row로 쌓입니다. "
            + "imageUrl은 POST /api/files/ROADMAP으로 미리 업로드해 받은 URL을 담아 보냅니다. ADMIN, STAFF 권한이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "추가 성공"),
            @ApiResponse(responseCode = "400", description = "요청값 검증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @PostMapping
    public ResponseEntity<RoadmapResponseDto> addRoadmap(@RequestBody @Valid RoadmapRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roadmapService.addRoadmap(request));
    }

    @Operation(summary = "로드맵 이미지 조회", description = "가장 최근에 추가된 로드맵 이미지를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "등록된 로드맵 이미지가 없음")
    })
    @GetMapping
    public ResponseEntity<RoadmapResponseDto> getLatestRoadmap() {
        return ResponseEntity.ok(roadmapService.getLatestRoadmap());
    }
}

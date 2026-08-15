package com.example.cau_likelion_spring.intro.controller;

import com.example.cau_likelion_spring.intro.dto.TrackRequestDto;
import com.example.cau_likelion_spring.intro.dto.TrackResponseDto;
import com.example.cau_likelion_spring.intro.service.TrackService;
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

@Tag(name = "Track (Admin)", description = "랜딩페이지 트랙 소개 생성/조회/수정/삭제 API")
@RestController
@RequestMapping("/api/admin/tracks")
@RequiredArgsConstructor
public class TrackController {

    private final TrackService trackService;

    @Operation(summary = "트랙 소개 생성", description = "ADMIN 권한이 필요합니다.")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<TrackResponseDto> createTrack(@Valid @RequestBody TrackRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(trackService.createTrack(request));
    }

    @Operation(summary = "트랙 소개 리스트 조회")
    @GetMapping
    public ResponseEntity<List<TrackResponseDto>> getTrackList() {
        return ResponseEntity.ok(trackService.getTrackList());
    }

    @Operation(summary = "트랙 소개 수정", description = "요청 바디로 전체 필드를 덮어씁니다 (부분 수정 아님). ADMIN 권한이 필요합니다.")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<TrackResponseDto> updateTrack(
            @Parameter(description = "트랙 id") @PathVariable Long id,
            @Valid @RequestBody TrackRequestDto request) {
        return ResponseEntity.ok(trackService.updateTrack(id, request));
    }

    @Operation(summary = "트랙 소개 삭제", description = "ADMIN 권한이 필요합니다.")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrack(@Parameter(description = "트랙 id") @PathVariable Long id) {
        trackService.deleteTrack(id);
        return ResponseEntity.noContent().build();
    }
}

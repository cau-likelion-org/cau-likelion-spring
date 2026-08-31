package com.example.cau_likelion_spring.organization.controller;

import com.example.cau_likelion_spring.organization.dto.GenerationCreateRequestDto;
import com.example.cau_likelion_spring.organization.dto.GenerationListResponseDto;
import com.example.cau_likelion_spring.organization.service.GenerationService;
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

@Tag(name = "Generation (Admin)", description = "기수/파트 생성·조회 API")
@RestController
@RequestMapping("/api/generations")
@RequiredArgsConstructor
public class GenerationController {

    private final GenerationService generationService;

    @Operation(summary = "기수 생성", description = "기수 번호, 활동 년도, 파트명 리스트를 받아 기수와 파트를 한 번에 생성합니다. ADMIN 권한이 필요합니다.")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRESIDENT')")
    @PostMapping
    public ResponseEntity<GenerationListResponseDto> createGeneration(
            @Valid @RequestBody GenerationCreateRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(generationService.createGeneration(request));
    }

    @Operation(summary = "전체 기수 리스트 조회", description = "각 기수의 번호, 활동 년도, 소속 파트 목록을 함께 조회합니다.")
    @GetMapping
    public ResponseEntity<List<GenerationListResponseDto>> getGenerationList() {
        return ResponseEntity.ok(generationService.getGenerationList());
    }

    @Operation(summary = "현재 기수 전환", description = "해당 id의 기수를 현재 활동 기수(status=IN_ACTIVITY)로 전환합니다. 기존에 활동 중이던 기수가 있다면 활동 후(status=AFTER_ACTIVITY) 상태로 바뀝니다. ADMIN 권한이 필요합니다.")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRESIDENT')")
    @PatchMapping("/{id}/current")
    public ResponseEntity<Void> changeCurrentGeneration(
            @Parameter(description = "현재 기수로 지정할 기수 id") @PathVariable Long id) {
        generationService.changeCurrentGeneration(id);
        return ResponseEntity.noContent().build();
    }
}

package com.example.cau_likelion_spring.intro.controller;

import com.example.cau_likelion_spring.intro.dto.CurriculumRequestDto;
import com.example.cau_likelion_spring.intro.dto.CurriculumResponseDto;
import com.example.cau_likelion_spring.intro.service.CurriculumService;
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

@Tag(name = "Curriculum (Admin)", description = "커리큘럼 생성/조회/수정/삭제 API")
@RestController
@RequestMapping("/api/admin/curriculums")
@RequiredArgsConstructor
public class CurriculumController {

    private final CurriculumService curriculumService;

    @Operation(summary = "커리큘럼 생성", description = "trackId는 필수이며, 존재하지 않는 트랙이면 404가 반환됩니다. ADMIN 권한이 필요합니다.")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRESIDENT')")
    @PostMapping
    public ResponseEntity<CurriculumResponseDto> createCurriculum(
            @Valid @RequestBody CurriculumRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(curriculumService.createCurriculum(request));
    }

    @Operation(summary = "커리큘럼 리스트 조회")
    @GetMapping
    public ResponseEntity<List<CurriculumResponseDto>> getCurriculumList() {
        return ResponseEntity.ok(curriculumService.getCurriculumList());
    }

    @Operation(summary = "커리큘럼 수정", description = "요청 바디로 전체 필드를 덮어씁니다 (부분 수정 아님). trackId도 필수입니다. ADMIN 권한이 필요합니다.")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRESIDENT')")
    @PutMapping("/{id}")
    public ResponseEntity<CurriculumResponseDto> updateCurriculum(
            @Parameter(description = "커리큘럼 id") @PathVariable Long id,
            @Valid @RequestBody CurriculumRequestDto request) {
        return ResponseEntity.ok(curriculumService.updateCurriculum(id, request));
    }

    @Operation(summary = "커리큘럼 삭제", description = "ADMIN 권한이 필요합니다.")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRESIDENT')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCurriculum(@Parameter(description = "커리큘럼 id") @PathVariable Long id) {
        curriculumService.deleteCurriculum(id);
        return ResponseEntity.noContent().build();
    }
}

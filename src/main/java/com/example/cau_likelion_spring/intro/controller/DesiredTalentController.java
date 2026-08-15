package com.example.cau_likelion_spring.intro.controller;

import com.example.cau_likelion_spring.intro.dto.DesiredTalentRequestDto;
import com.example.cau_likelion_spring.intro.dto.DesiredTalentResponseDto;
import com.example.cau_likelion_spring.intro.service.DesiredTalentService;
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

@Tag(name = "DesiredTalent (Admin)", description = "공통/파트별 인재상 생성/조회/수정/삭제 API")
@RestController
@RequestMapping("/api/admin/desired-talents")
@RequiredArgsConstructor
public class DesiredTalentController {

    private final DesiredTalentService desiredTalentService;

    @Operation(summary = "인재상 생성", description = "partName을 비워두면 공통 인재상, 값을 넣으면 해당 파트의 인재상으로 생성됩니다. ADMIN 권한이 필요합니다.")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<DesiredTalentResponseDto> createDesiredTalent(
            @Valid @RequestBody DesiredTalentRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(desiredTalentService.createDesiredTalent(request));
    }

    @Operation(summary = "인재상 리스트 조회")
    @GetMapping
    public ResponseEntity<List<DesiredTalentResponseDto>> getDesiredTalentList() {
        return ResponseEntity.ok(desiredTalentService.getDesiredTalentList());
    }

    @Operation(summary = "인재상 수정", description = "요청 바디로 전체 필드를 덮어씁니다 (부분 수정 아님). ADMIN 권한이 필요합니다.")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<DesiredTalentResponseDto> updateDesiredTalent(
            @Parameter(description = "인재상 id") @PathVariable Long id,
            @Valid @RequestBody DesiredTalentRequestDto request) {
        return ResponseEntity.ok(desiredTalentService.updateDesiredTalent(id, request));
    }

    @Operation(summary = "인재상 삭제", description = "ADMIN 권한이 필요합니다.")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDesiredTalent(@Parameter(description = "인재상 id") @PathVariable Long id) {
        desiredTalentService.deleteDesiredTalent(id);
        return ResponseEntity.noContent().build();
    }
}

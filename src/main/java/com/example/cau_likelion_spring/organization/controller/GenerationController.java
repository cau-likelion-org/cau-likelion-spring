package com.example.cau_likelion_spring.organization.controller;

import com.example.cau_likelion_spring.organization.dto.GenerationListResponseDto;
import com.example.cau_likelion_spring.organization.service.GenerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Generation", description = "기수 조회 API")
@RestController
@RequestMapping("/api/admin/generations")
@RequiredArgsConstructor
public class GenerationController {

    private final GenerationService generationService;

    @Operation(summary = "전체 기수 리스트 조회", description = "각 기수의 번호, 활동 년도, 소속 파트 목록을 함께 조회합니다.")
    @GetMapping
    public ResponseEntity<List<GenerationListResponseDto>> getGenerationList() {
        return ResponseEntity.ok(generationService.getGenerationList());
    }
}

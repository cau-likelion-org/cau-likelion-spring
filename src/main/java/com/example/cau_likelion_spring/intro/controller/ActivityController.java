package com.example.cau_likelion_spring.intro.controller;

import com.example.cau_likelion_spring.intro.dto.ActivityRequestDto;
import com.example.cau_likelion_spring.intro.dto.ActivityResponseDto;
import com.example.cau_likelion_spring.intro.service.ActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Activity (Admin)", description = "랜딩페이지 활동 소개 생성/조회/수정/삭제 API")
@RestController
@RequestMapping("/api/admin/activities")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    @Operation(summary = "활동 소개 생성")
    @PostMapping
    public ResponseEntity<ActivityResponseDto> createActivity(@Valid @RequestBody ActivityRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(activityService.createActivity(request));
    }

    @Operation(summary = "활동 소개 리스트 조회")
    @GetMapping
    public ResponseEntity<List<ActivityResponseDto>> getActivityList() {
        return ResponseEntity.ok(activityService.getActivityList());
    }

    @Operation(summary = "활동 소개 수정", description = "요청 바디로 전체 필드를 덮어씁니다 (부분 수정 아님). pageNavigation은 선택 항목입니다.")
    @PutMapping("/{id}")
    public ResponseEntity<ActivityResponseDto> updateActivity(
            @Parameter(description = "활동 id") @PathVariable Long id,
            @Valid @RequestBody ActivityRequestDto request) {
        return ResponseEntity.ok(activityService.updateActivity(id, request));
    }

    @Operation(summary = "활동 소개 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteActivity(@Parameter(description = "활동 id") @PathVariable Long id) {
        activityService.deleteActivity(id);
        return ResponseEntity.noContent().build();
    }
}

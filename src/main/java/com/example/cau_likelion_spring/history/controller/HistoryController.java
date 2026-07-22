package com.example.cau_likelion_spring.history.controller;

import com.example.cau_likelion_spring.history.dto.HistoryCreateRequest;
import com.example.cau_likelion_spring.history.dto.HistoryDetailResponse;
import com.example.cau_likelion_spring.history.dto.HistoryListResponse;
import com.example.cau_likelion_spring.history.dto.HistoryUpdateRequest;
import com.example.cau_likelion_spring.history.service.HistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "History (갤러리 - 추억)", description = "갤러리 '추억' 게시물 조회/등록/수정/삭제 API")
@RestController
@RequestMapping("/api/histories")
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryService historyService;

    @Operation(summary = "추억 목록 조회", description = "시작일 최신순으로 추억 게시물 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<List<HistoryListResponse>> getList() {
        return ResponseEntity.ok(historyService.getList());
    }

    @Operation(summary = "추억 상세 조회", description = "게시물 ID로 추억 게시물 상세 정보를 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<HistoryDetailResponse> getDetail(
            @Parameter(description = "추억 게시물 ID") @PathVariable Long id
    ) {
        return ResponseEntity.ok(historyService.getDetail(id));
    }

    @Operation(summary = "추억 게시물 등록", description = "새 추억 게시물을 등록합니다. 운영진/admin만 가능합니다.")
    @PostMapping
    public ResponseEntity<HistoryDetailResponse> create(@Valid @RequestBody HistoryCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(historyService.create(request));
    }

    @Operation(summary = "추억 게시물 수정", description = "등록된 추억 게시물을 수정합니다. 운영진/admin만 가능합니다.")
    @PutMapping("/{id}")
    public ResponseEntity<HistoryDetailResponse> update(
            @Parameter(description = "추억 게시물 ID") @PathVariable Long id,
            @Valid @RequestBody HistoryUpdateRequest request
    ) {
        return ResponseEntity.ok(historyService.update(id, request));
    }

    @Operation(summary = "추억 게시물 삭제", description = "등록된 추억 게시물을 삭제합니다. 운영진/admin만 가능합니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "추억 게시물 ID") @PathVariable Long id
    ) {
        historyService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

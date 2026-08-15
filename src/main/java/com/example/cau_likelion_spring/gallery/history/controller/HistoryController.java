package com.example.cau_likelion_spring.gallery.history.controller;

import com.example.cau_likelion_spring.gallery.history.dto.HistoryCreateRequest;
import com.example.cau_likelion_spring.gallery.history.dto.HistoryDetailResponse;
import com.example.cau_likelion_spring.gallery.history.dto.HistoryListResponse;
import com.example.cau_likelion_spring.gallery.history.dto.HistoryUpdateRequest;
import com.example.cau_likelion_spring.gallery.history.service.HistoryService;
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
@RequestMapping("/api/gallery/histories")
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

    @Operation(summary = "추억 게시물 등록", description = "새 추억 게시물을 등록합니다. 운영진/admin만 가능합니다. "
            + "thumbnailUrl/imageUrls는 POST /api/files/HISTORY로 미리 업로드한 URL을 담아 보냅니다(imageUrls는 최소 1장, 최대 10장).")
    @PostMapping
    public ResponseEntity<HistoryDetailResponse> create(@RequestBody @Valid HistoryCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(historyService.create(request));
    }

    @Operation(summary = "추억 게시물 수정", description = "등록된 추억 게시물을 수정합니다. 운영진/admin만 가능합니다. "
            + "thumbnailUrl을 생략하면 기존 값을 유지합니다. imageUrls를 생략하면 기존 이미지를 유지하고, "
            + "값을 보내면(기존에 유지할 URL + POST /api/files/HISTORY로 새로 업로드한 URL) 그 목록으로 전체 교체합니다.")
    @PutMapping("/{id}")
    public ResponseEntity<HistoryDetailResponse> update(
            @Parameter(description = "추억 게시물 ID") @PathVariable Long id,
            @RequestBody @Valid HistoryUpdateRequest request
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

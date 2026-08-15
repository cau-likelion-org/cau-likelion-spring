package com.example.cau_likelion_spring.gallery.project.controller;

import com.example.cau_likelion_spring.gallery.project.dto.ProjectGalleryCreateRequest;
import com.example.cau_likelion_spring.gallery.project.dto.ProjectGalleryDetailResponse;
import com.example.cau_likelion_spring.gallery.project.dto.ProjectGalleryListResponse;
import com.example.cau_likelion_spring.gallery.project.dto.ProjectGalleryUpdateRequest;
import com.example.cau_likelion_spring.gallery.project.service.ProjectGalleryService;
import com.example.cau_likelion_spring.project.domain.ProjectCategory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Project (갤러리 - 프로젝트)", description = "갤러리 '프로젝트' 게시물 조회/등록/수정/삭제 API")
@RestController
@RequestMapping("/api/gallery/projects")
@RequiredArgsConstructor
public class ProjectGalleryController {

    private final ProjectGalleryService projectGalleryService;

    @Operation(summary = "프로젝트 게시물 목록 조회", description = "기수 번호 / 프로젝트 구분으로 필터링할 수 있습니다. 둘 다 생략하면 전체를 시작일 최신순으로 조회합니다.")
    @GetMapping
    public ResponseEntity<List<ProjectGalleryListResponse>> getList(
            @Parameter(description = "기수 번호 (예: 13)") @RequestParam(required = false) Integer generationNumber,
            @Parameter(description = "프로젝트 구분") @RequestParam(required = false) ProjectCategory category) {
        return ResponseEntity.ok(projectGalleryService.getList(generationNumber, category));
    }

    @Operation(summary = "프로젝트 게시물 상세 조회", description = "게시물 ID로 프로젝트 게시물 상세 정보를 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<ProjectGalleryDetailResponse> getDetail(
            @Parameter(description = "프로젝트 게시물 ID") @PathVariable Long id) {
        return ResponseEntity.ok(projectGalleryService.getDetail(id));
    }

    @Operation(summary = "프로젝트 게시물 등록", description = "새 프로젝트 게시물을 등록합니다. 운영진/admin만 가능합니다. "
            + "thumbnailUrl/imageUrls는 POST /api/files/PROJECT로 미리 업로드한 URL을 담아 보냅니다(imageUrls는 최소 1장, 최대 10장).")
    @PostMapping
    public ResponseEntity<ProjectGalleryDetailResponse> create(@RequestBody @Valid ProjectGalleryCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectGalleryService.create(request));
    }

    @Operation(summary = "프로젝트 게시물 수정", description = "등록된 프로젝트 게시물을 수정합니다. 운영진/admin만 가능합니다. "
            + "thumbnailUrl을 생략하면 기존 값을 유지합니다. imageUrls를 생략하면 기존 이미지를 유지하고, "
            + "값을 보내면(기존에 유지할 URL + POST /api/files/PROJECT로 새로 업로드한 URL) 그 목록으로 전체 교체합니다.")
    @PutMapping("/{id}")
    public ResponseEntity<ProjectGalleryDetailResponse> update(
            @Parameter(description = "프로젝트 게시물 ID") @PathVariable Long id,
            @RequestBody @Valid ProjectGalleryUpdateRequest request) {
        return ResponseEntity.ok(projectGalleryService.update(id, request));
    }

    @Operation(summary = "프로젝트 게시물 삭제", description = "등록된 프로젝트 게시물을 삭제합니다. 운영진/admin만 가능합니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "프로젝트 게시물 ID") @PathVariable Long id) {
        projectGalleryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

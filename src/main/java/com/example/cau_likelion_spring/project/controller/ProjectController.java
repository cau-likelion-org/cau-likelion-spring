package com.example.cau_likelion_spring.project.controller;

import com.example.cau_likelion_spring.project.domain.ProjectCategory;
import com.example.cau_likelion_spring.project.dto.ProjectRequest;
import com.example.cau_likelion_spring.project.dto.ProjectResponse;
import com.example.cau_likelion_spring.project.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Project", description = "프로젝트 API")
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @Operation(summary = "프로젝트 생성", description = "새로운 프로젝트를 이미지/링크/멤버 정보와 함께 생성합니다. ADMIN, STAFF 권한이 필요합니다. "
            + "banner는 프로젝트 하이라이트에 노출되는 텍스트 캡션이며(선택), images는 POST /api/files/PROJECT로 미리 업로드해 받은 URL을 담아 보냅니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "요청값 검증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 기수(generationId)")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'PRESIDENT')")
    @PostMapping
    public ResponseEntity<ProjectResponse> create(@RequestBody @Valid ProjectRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.create(request));
    }

    @Operation(summary = "프로젝트 목록 조회",
            description = "프로젝트 목록을 조회합니다. 기수/카테고리로 필터링할 수 있으며, 생성일 기준 내림차순으로 정렬됩니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getAll(
            @Parameter(description = "기수 ID로 필터링") @RequestParam(required = false) Long generationId,
            @Parameter(description = "카테고리로 필터링") @RequestParam(required = false) ProjectCategory category) {
        return ResponseEntity.ok(projectService.getAll(generationId, category));
    }

    @Operation(summary = "프로젝트 단건 조회", description = "프로젝트 ID로 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 프로젝트")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getById(
            @Parameter(description = "프로젝트 ID", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(projectService.getById(id));
    }

    @Operation(summary = "프로젝트 수정", description = "프로젝트 정보를 수정합니다. 링크/멤버/이미지는 요청 내용으로 전체 대체됩니다. "
            + "images에 기존에 유지할 이미지의 URL과 새로 추가한 이미지의 URL(POST /api/files/PROJECT로 미리 업로드)을 함께 담아 보내면 그 목록으로 전체 교체됩니다. "
            + "ADMIN, STAFF 권한이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "요청값 검증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 프로젝트 또는 기수(generationId)")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'PRESIDENT')")
    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> update(
            @Parameter(description = "프로젝트 ID", required = true) @PathVariable Long id,
            @RequestBody @Valid ProjectRequest request) {
        return ResponseEntity.ok(projectService.update(id, request));
    }

    @Operation(summary = "프로젝트 삭제", description = "프로젝트와 연관된 이미지/링크/멤버를 함께 삭제합니다. ADMIN, STAFF 권한이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 프로젝트")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'PRESIDENT')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "프로젝트 ID", required = true) @PathVariable Long id) {
        projectService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

package com.example.cau_likelion_spring.blog.controller;

import com.example.cau_likelion_spring.blog.domain.BlogCategory;
import com.example.cau_likelion_spring.blog.dto.BlogRequest;
import com.example.cau_likelion_spring.blog.dto.BlogResponse;
import com.example.cau_likelion_spring.blog.service.BlogService;
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

@Tag(name = "Blog", description = "블로그 API")
@RestController
@RequestMapping("/api/blogs")
@RequiredArgsConstructor
public class BlogController {

    private final BlogService blogService;

    @Operation(summary = "블로그 생성",
            description = "기수/작성자/URL/카테고리를 입력받아 생성합니다. 제목·썸네일·요약은 URL을 스크래핑해서 채워집니다. ADMIN 권한이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "요청값 검증 실패 또는 유효하지 않은 URL"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 기수(generationId)"),
            @ApiResponse(responseCode = "502", description = "대상 페이지를 불러올 수 없음")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<BlogResponse> create(@Valid @RequestBody BlogRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(blogService.create(request));
    }

    @Operation(summary = "블로그 목록 조회",
            description = "블로그 목록을 조회합니다. 기수/카테고리로 필터링할 수 있으며, 업로드일 기준 내림차순으로 정렬됩니다. "
                    + "카드 클릭 시에는 응답의 url 필드로 원문 페이지에 바로 이동시키면 됩니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    public ResponseEntity<List<BlogResponse>> getAll(
            @Parameter(description = "기수 ID로 필터링") @RequestParam(required = false) Long generationId,
            @Parameter(description = "카테고리로 필터링") @RequestParam(required = false) BlogCategory category) {
        return ResponseEntity.ok(blogService.getAll(generationId, category));
    }

    @Operation(summary = "블로그 단건 조회", description = "블로그 ID로 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 블로그")
    })
    @GetMapping("/{id:[0-9]+}")
    public ResponseEntity<BlogResponse> getById(
            @Parameter(description = "블로그 ID", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(blogService.getById(id));
    }

    @Operation(summary = "블로그 수정",
            description = "블로그 정보를 수정합니다. 제목·썸네일·요약은 URL을 다시 스크래핑해서 갱신됩니다. ADMIN 권한이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "요청값 검증 실패 또는 유효하지 않은 URL"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 블로그 또는 기수(generationId)"),
            @ApiResponse(responseCode = "502", description = "대상 페이지를 불러올 수 없음")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id:[0-9]+}")
    public ResponseEntity<BlogResponse> update(
            @Parameter(description = "블로그 ID", required = true) @PathVariable Long id,
            @Valid @RequestBody BlogRequest request) {
        return ResponseEntity.ok(blogService.update(id, request));
    }

    @Operation(summary = "블로그 삭제", description = "블로그를 삭제합니다. ADMIN 권한이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 블로그")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id:[0-9]+}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "블로그 ID", required = true) @PathVariable Long id) {
        blogService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

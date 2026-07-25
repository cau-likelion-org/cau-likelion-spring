package com.example.cau_likelion_spring.blog.controller;

import com.example.cau_likelion_spring.blog.dto.LinkPreviewDto;
import com.example.cau_likelion_spring.blog.service.LinkPreviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Link Preview", description = "블로그 링크 미리보기 API")
@RestController
@RequestMapping("/api/preview")
@RequiredArgsConstructor
public class LinkPreviewController {

    private final LinkPreviewService linkPreviewService;

    @Operation(summary = "블로그 링크 미리보기",
            description = "블로그 URL에서 썸네일 이미지, 초반 본문 텍스트(3줄), 작성일을 추출합니다. ADMIN, STAFF 권한이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 URL"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "502", description = "대상 페이지를 불러올 수 없음")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @GetMapping
    public ResponseEntity<LinkPreviewDto> preview(
            @Parameter(description = "미리보기를 생성할 블로그 URL", required = true) @RequestParam String url) {
        return ResponseEntity.ok(linkPreviewService.preview(url));
    }
}

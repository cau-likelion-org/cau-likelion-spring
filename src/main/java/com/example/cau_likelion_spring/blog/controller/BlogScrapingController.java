package com.example.cau_likelion_spring.blog.controller;

import com.example.cau_likelion_spring.blog.dto.BlogScrapingResponse;
import com.example.cau_likelion_spring.blog.service.BlogScrapingService;
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

@Tag(name = "Blog", description = "블로그 API")
@RestController
@RequestMapping("/api/blogs/scraping")
@RequiredArgsConstructor
public class BlogScrapingController {

    private final BlogScrapingService blogScrapingService;

    @Operation(summary = "블로그 링크 스크래핑",
            description = "블로그 URL에서 썸네일 이미지, 초반 본문 텍스트(3줄), 작성일을 추출합니다. ADMIN, STAFF 권한이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 URL"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "502", description = "대상 페이지를 불러올 수 없음")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @GetMapping
    public ResponseEntity<BlogScrapingResponse> scrape(
            @Parameter(description = "스크래핑할 블로그 URL", required = true) @RequestParam String url) {
        return ResponseEntity.ok(blogScrapingService.scrape(url));
    }
}

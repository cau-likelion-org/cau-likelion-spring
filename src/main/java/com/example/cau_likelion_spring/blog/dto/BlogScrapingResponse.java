package com.example.cau_likelion_spring.blog.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "블로그 링크 스크래핑 응답")
public record BlogScrapingResponse(

        @Schema(description = "원문 블로그 URL")
        String url,

        @Schema(description = "제목")
        String title,

        @Schema(description = "썸네일 이미지 URL")
        String thumbnailUrl,

        @Schema(description = "초반 본문 텍스트 (최대 150자, 공백 포함)")
        String description,

        @Schema(description = "작성일 (yyyy-MM-dd)")
        String publishedDate
) {
}

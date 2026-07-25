package com.example.cau_likelion_spring.blog.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "블로그 링크 미리보기 응답")
public record LinkPreviewDto(

        @Schema(description = "원문 블로그 URL")
        String url,

        @Schema(description = "제목")
        String title,

        @Schema(description = "썸네일 이미지 URL")
        String thumbnailUrl,

        @Schema(description = "초반 본문 텍스트 (최대 3줄)")
        String description,

        @Schema(description = "작성일 (yyyy-MM-dd)")
        String publishedDate
) {
}

package com.example.cau_likelion_spring.blog.dto;

import com.example.cau_likelion_spring.blog.domain.Blog;
import com.example.cau_likelion_spring.blog.domain.BlogCategory;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "블로그 응답")
public record BlogResponse(

        @Schema(description = "블로그 ID", example = "1")
        Long id,

        @Schema(description = "기수 ID", example = "1")
        Long generationId,

        @Schema(description = "기수 번호", example = "9")
        Integer generationNumber,

        @Schema(description = "제목 (스크래핑됨)")
        String title,

        @Schema(description = "썸네일 이미지 URL (스크래핑됨)")
        String thumbnailUrl,

        @Schema(description = "블로그 카테고리")
        BlogCategory category,

        @Schema(description = "요약 (스크래핑됨)")
        String summary,

        @Schema(description = "작성자")
        String writer,

        @Schema(description = "원문 블로그 URL (클릭 시 이 URL로 바로 이동)")
        String url,

        @Schema(description = "업로드 일시")
        LocalDateTime createdAt
) {

    public static BlogResponse of(Blog blog) {
        return new BlogResponse(
                blog.getId(),
                blog.getGeneration().getId(),
                blog.getGeneration().getNumber(),
                blog.getTitle(),
                blog.getThumbnailUrl(),
                blog.getCategory(),
                blog.getSummary(),
                blog.getWriter(),
                blog.getUrl(),
                blog.getCreatedAt()
        );
    }
}

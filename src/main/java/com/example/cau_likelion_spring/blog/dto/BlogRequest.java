package com.example.cau_likelion_spring.blog.dto;

import com.example.cau_likelion_spring.blog.domain.BlogCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "블로그 생성/수정 요청")
public record BlogRequest(

        @Schema(description = "기수 ID", example = "1")
        @NotNull Long generationId,

        @Schema(description = "작성자", example = "홍길동")
        @NotBlank String writer,

        @Schema(description = "원문 블로그 URL (title/썸네일/요약을 이 URL에서 스크래핑함)")
        @NotBlank String url,

        @Schema(description = "블로그 카테고리")
        @NotNull BlogCategory category
) {
}

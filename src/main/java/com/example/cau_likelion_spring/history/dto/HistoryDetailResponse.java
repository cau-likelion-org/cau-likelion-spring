package com.example.cau_likelion_spring.history.dto;

import com.example.cau_likelion_spring.history.domain.History;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

public record HistoryDetailResponse(

        @Schema(description = "게시물 ID")
        Long id,

        @Schema(description = "게시물 제목")
        String title,

        @Schema(description = "기수 번호")
        Integer generationNumber,

        @Schema(description = "상세 텍스트")
        String description,

        @Schema(description = "시작일")
        LocalDate startDate,

        @Schema(description = "종료일 (하루짜리 일정이면 null)")
        LocalDate endDate,

        @Schema(description = "이미지 URL 목록 (첫 번째가 대표사진)")
        List<String> imageUrls
) {

    public static HistoryDetailResponse of(History history, List<String> imageUrls) {
        return new HistoryDetailResponse(
                history.getId(),
                history.getTitle(),
                history.getGeneration().getNumber(),
                history.getDescription(),
                history.getStartDate(),
                history.getEndDate(),
                imageUrls
        );
    }
}

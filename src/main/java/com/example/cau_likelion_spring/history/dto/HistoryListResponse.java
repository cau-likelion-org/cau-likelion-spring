package com.example.cau_likelion_spring.history.dto;

import com.example.cau_likelion_spring.history.domain.History;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public record HistoryListResponse(

        @Schema(description = "게시물 ID")
        Long id,

        @Schema(description = "대표 이미지 URL")
        String thumbnailUrl,

        @Schema(description = "게시물 제목")
        String title,

        @Schema(description = "기수 번호")
        Integer generationNumber,

        @Schema(description = "시작일")
        LocalDate startDate,

        @Schema(description = "종료일 (하루짜리 일정이면 null)")
        LocalDate endDate
) {

    public static HistoryListResponse from(History history) {
        return new HistoryListResponse(
                history.getId(),
                history.getThumbnailUrl(),
                history.getTitle(),
                history.getGeneration().getNumber(),
                history.getStartDate(),
                history.getEndDate()
        );
    }
}

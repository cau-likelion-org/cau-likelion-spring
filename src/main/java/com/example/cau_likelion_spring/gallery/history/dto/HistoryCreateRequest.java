package com.example.cau_likelion_spring.gallery.history.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

/** thumbnailUrl/imageUrls는 POST /api/files/HISTORY로 미리 업로드해 받은 URL을 담는다. */
public record HistoryCreateRequest(

        @Schema(description = "기수 ID", example = "1")
        @NotNull(message = "기수는 필수입니다.")
        Long generationId,

        @Schema(description = "게시물 제목", example = "13기 여름 MT")
        @NotBlank(message = "제목은 필수입니다.")
        String title,

        @Schema(description = "상세 텍스트", example = "13기 다같이 강릉으로 여름 MT를 다녀왔습니다.")
        String description,

        @Schema(description = "시작일", example = "2026-08-01")
        @NotNull(message = "시작일은 필수입니다.")
        LocalDate startDate,

        @Schema(description = "종료일 (하루짜리 일정이면 생략)", example = "2026-08-02")
        LocalDate endDate,

        @Schema(description = "대표 이미지 URL (POST /api/files/HISTORY로 미리 업로드한 URL, 선택)")
        String thumbnailUrl,

        @Schema(description = "이미지 URL 목록 (POST /api/files/HISTORY로 미리 업로드한 URL, 최소 1장, 최대 10장)")
        @NotEmpty(message = "사진을 최소 1장 등록해주세요.")
        @Size(max = 10, message = "사진은 최대 10장까지 등록 가능합니다.")
        List<String> imageUrls
) {
}

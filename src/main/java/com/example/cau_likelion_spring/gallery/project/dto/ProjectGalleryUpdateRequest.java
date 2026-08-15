package com.example.cau_likelion_spring.gallery.project.dto;

import com.example.cau_likelion_spring.project.domain.ProjectCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

/** thumbnailUrl/imageUrls는 POST /api/files/PROJECT로 미리 업로드해 받은 URL을 담는다. 각각 생략하면 기존 값을 유지한다. */
public record ProjectGalleryUpdateRequest(

        @Schema(description = "기수 ID", example = "1")
        @NotNull(message = "기수는 필수입니다.")
        Long generationId,

        @Schema(description = "프로젝트 구분", example = "HACKATHON")
        @NotNull(message = "프로젝트 구분은 필수입니다.")
        ProjectCategory category,

        @Schema(description = "게시물 제목", example = "2026 중앙해커톤 본선 진출")
        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 70, message = "제목은 최대 70자까지 입력 가능합니다.")
        String title,

        @Schema(description = "상세 텍스트", example = "13기 백엔드 파트가 중앙해커톤 본선에 진출했습니다.")
        @Size(max = 300, message = "내용은 최대 300자까지 입력 가능합니다.")
        String description,

        @Schema(description = "시작일", example = "2026-08-01")
        @NotNull(message = "시작일은 필수입니다.")
        LocalDate startDate,

        @Schema(description = "종료일 (하루짜리 일정이면 생략)", example = "2026-08-02")
        LocalDate endDate,

        @Schema(description = "대표 이미지 URL (POST /api/files/PROJECT로 미리 업로드한 URL). 생략하면 기존 값 유지")
        String thumbnailUrl,

        @Schema(description = "이미지 URL 목록 (POST /api/files/PROJECT로 미리 업로드한 URL, 최대 10장). 생략하면 기존 이미지 유지, 값을 보내면 전체 교체")
        @Size(max = 10, message = "사진은 최대 10장까지 등록 가능합니다.")
        List<String> imageUrls
) {
}

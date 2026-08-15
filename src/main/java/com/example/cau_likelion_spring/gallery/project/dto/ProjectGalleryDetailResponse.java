package com.example.cau_likelion_spring.gallery.project.dto;

import com.example.cau_likelion_spring.gallery.project.domain.ProjectGallery;
import com.example.cau_likelion_spring.project.domain.ProjectCategory;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

public record ProjectGalleryDetailResponse(

        @Schema(description = "게시물 ID")
        Long id,

        @Schema(description = "게시물 제목")
        String title,

        @Schema(description = "기수 번호")
        Integer generationNumber,

        @Schema(description = "프로젝트 구분")
        ProjectCategory category,

        @Schema(description = "프로젝트 구분 설명", example = "해커톤")
        String categoryDescription,

        @Schema(description = "상세 텍스트")
        String description,

        @Schema(description = "시작일")
        LocalDate startDate,

        @Schema(description = "종료일 (하루짜리 일정이면 null)")
        LocalDate endDate,

        @Schema(description = "이미지 URL 목록 (첫 번째가 대표사진)")
        List<String> imageUrls
) {

    public static ProjectGalleryDetailResponse of(ProjectGallery projectGallery, List<String> imageUrls) {
        return new ProjectGalleryDetailResponse(
                projectGallery.getId(),
                projectGallery.getTitle(),
                projectGallery.getGeneration().getNumber(),
                projectGallery.getCategory(),
                projectGallery.getCategory().getDescription(),
                projectGallery.getDescription(),
                projectGallery.getStartDate(),
                projectGallery.getEndDate(),
                imageUrls
        );
    }
}

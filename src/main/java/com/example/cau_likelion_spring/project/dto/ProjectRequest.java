package com.example.cau_likelion_spring.project.dto;

import com.example.cau_likelion_spring.project.domain.Platform;
import com.example.cau_likelion_spring.project.domain.ProjectCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

/** images는 POST /api/files/PROJECT로 미리 업로드해 받은 URL을 이 DTO에 담아 넘긴다. banner는 이미지가 아니라 랜딩페이지 프로젝트 하이라이트에 노출되는 텍스트 캡션(예: "2026 멋대 최우수 출품작")이다. */
@Schema(description = "프로젝트 생성/수정 요청")
public record ProjectRequest(

        @Schema(description = "기수 ID", example = "1")
        @NotNull Long generationId,

        @Schema(description = "프로젝트 제목", example = "중하하 홈페이지 리뉴얼")
        @NotBlank String title,

        @Schema(description = "프로젝트 카테고리")
        ProjectCategory category,

        @Schema(description = "기술 스택", example = "Spring Boot, React")
        String stack,

        @Schema(description = "한줄 소개")
        String tagline,

        @Schema(description = "서비스 요약")
        String summary,

        @Schema(description = "팀명")
        String teamName,

        @Schema(description = "시작일")
        LocalDate startDate,

        @Schema(description = "종료일")
        LocalDate endDate,

        @Schema(description = "프로젝트 하이라이트 텍스트 배너", example = "2026 멋대 최우수 출품작")
        String banner,

        @Schema(description = "프로젝트 이미지 목록 (POST /api/files/PROJECT로 미리 업로드한 URL을 담는다)")
        List<ImageRequest> images,

        @Schema(description = "프로젝트 링크 목록")
        List<LinkRequest> links,

        @Schema(description = "프로젝트 참여 멤버 목록")
        List<MemberRequest> members
) {

    @Schema(description = "프로젝트 이미지 요청")
    public record ImageRequest(
            @Schema(description = "이미지 URL")
            @NotBlank String imageUrl,

            @Schema(description = "대표 이미지 여부", example = "true")
            Boolean isMain,

            @Schema(description = "노출 순서", example = "0")
            Integer displayOrder
    ) {
    }

    @Schema(description = "프로젝트 링크 요청")
    public record LinkRequest(
            @Schema(description = "링크 플랫폼")
            Platform platform,

            @Schema(description = "링크 URL")
            @NotBlank String url
    ) {
    }

    @Schema(description = "프로젝트 멤버 요청")
    public record MemberRequest(
            @Schema(description = "멤버 이름", example = "홍길동")
            @NotBlank String name,

            @Schema(description = "담당 파트", example = "Backend")
            String part
    ) {
    }
}

package com.example.cau_likelion_spring.project.dto;

import com.example.cau_likelion_spring.project.domain.Platform;
import com.example.cau_likelion_spring.project.domain.Project;
import com.example.cau_likelion_spring.project.domain.ProjectCategory;
import com.example.cau_likelion_spring.project.domain.ProjectImage;
import com.example.cau_likelion_spring.project.domain.ProjectLink;
import com.example.cau_likelion_spring.project.domain.ProjectMember;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "프로젝트 응답")
public record ProjectResponse(

        @Schema(description = "프로젝트 ID", example = "1")
        Long id,

        @Schema(description = "기수 ID", example = "1")
        Long generationId,

        @Schema(description = "기수 번호", example = "9")
        Integer generationNumber,

        @Schema(description = "프로젝트 제목")
        String title,

        @Schema(description = "프로젝트 카테고리")
        ProjectCategory category,

        @Schema(description = "기술 스택")
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

        @Schema(description = "프로젝트 이미지 목록")
        List<ImageResponse> images,

        @Schema(description = "프로젝트 링크 목록")
        List<LinkResponse> links,

        @Schema(description = "프로젝트 참여 멤버 목록")
        List<MemberResponse> members,

        @Schema(description = "프로젝트 랜딩페이지 노출 여부")
        Boolean isExposed
) {

    @Schema(description = "프로젝트 이미지 응답")
    public record ImageResponse(
            @Schema(description = "이미지 ID")
            Long id,

            @Schema(description = "이미지 URL")
            String imageUrl,

            @Schema(description = "대표 이미지 여부")
            Boolean isMain,

            @Schema(description = "노출 순서")
            Integer displayOrder
    ) {
        static ImageResponse from(ProjectImage image) {
            return new ImageResponse(image.getId(), image.getImageUrl(), image.getIsMain(), image.getDisplayOrder());
        }
    }

    @Schema(description = "프로젝트 링크 응답")
    public record LinkResponse(
            @Schema(description = "링크 ID")
            Long id,

            @Schema(description = "링크 플랫폼")
            Platform platform,

            @Schema(description = "링크 URL")
            String url
    ) {
        static LinkResponse from(ProjectLink link) {
            return new LinkResponse(link.getId(), link.getPlatform(), link.getUrl());
        }
    }

    @Schema(name = "ProjectMemberResponse", description = "프로젝트 멤버 응답")

    public record MemberResponse(
            @Schema(description = "멤버 ID")
            Long id,

            @Schema(description = "멤버 이름")
            String name,

            @Schema(description = "담당 파트")
            String part
    ) {
        static MemberResponse from(ProjectMember member) {
            return new MemberResponse(member.getId(), member.getName(), member.getPart());
        }
    }

    public static ProjectResponse of(Project project, List<ProjectImage> images,
                                      List<ProjectLink> links, List<ProjectMember> members) {
        return new ProjectResponse(
                project.getId(),
                project.getGeneration().getId(),
                project.getGeneration().getNumber(),
                project.getTitle(),
                project.getCategory(),
                project.getStack(),
                project.getTagline(),
                project.getSummary(),
                project.getTeamName(),
                project.getStartDate(),
                project.getEndDate(),
                project.getBanner(),
                images.stream().map(ImageResponse::from).toList(),
                links.stream().map(LinkResponse::from).toList(),
                members.stream().map(MemberResponse::from).toList(),
                project.getIsExposed()
        );
    }
}

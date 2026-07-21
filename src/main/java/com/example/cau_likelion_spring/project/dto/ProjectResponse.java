package com.example.cau_likelion_spring.project.dto;

import com.example.cau_likelion_spring.project.domain.Platform;
import com.example.cau_likelion_spring.project.domain.Project;
import com.example.cau_likelion_spring.project.domain.ProjectCategory;
import com.example.cau_likelion_spring.project.domain.ProjectImage;
import com.example.cau_likelion_spring.project.domain.ProjectLink;
import com.example.cau_likelion_spring.project.domain.ProjectMember;

import java.time.LocalDate;
import java.util.List;

public record ProjectResponse(

        Long id,

        Long generationId,

        Integer generationNumber,

        String title,

        ProjectCategory category,

        String stack,

        String summary,

        String detail,

        LocalDate startDate,

        LocalDate endDate,

        List<ImageResponse> images,

        List<LinkResponse> links,

        List<MemberResponse> members
) {

    public record ImageResponse(
            Long id,
            String imageUrl,
            Boolean isMain,
            Integer displayOrder
    ) {
        static ImageResponse from(ProjectImage image) {
            return new ImageResponse(image.getId(), image.getImageUrl(), image.getIsMain(), image.getDisplayOrder());
        }
    }

    public record LinkResponse(
            Long id,
            Platform platform,
            String url
    ) {
        static LinkResponse from(ProjectLink link) {
            return new LinkResponse(link.getId(), link.getPlatform(), link.getUrl());
        }
    }

    public record MemberResponse(
            Long id,
            String name,
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
                project.getSummary(),
                project.getDetail(),
                project.getStartDate(),
                project.getEndDate(),
                images.stream().map(ImageResponse::from).toList(),
                links.stream().map(LinkResponse::from).toList(),
                members.stream().map(MemberResponse::from).toList()
        );
    }
}

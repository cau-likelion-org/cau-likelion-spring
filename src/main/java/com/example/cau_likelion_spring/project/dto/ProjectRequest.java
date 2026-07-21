package com.example.cau_likelion_spring.project.dto;

import com.example.cau_likelion_spring.project.domain.Platform;
import com.example.cau_likelion_spring.project.domain.ProjectCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record ProjectRequest(

        @NotNull Long generationId,

        @NotBlank String title,

        ProjectCategory category,

        String stack,

        String summary,

        String detail,

        LocalDate startDate,

        LocalDate endDate,

        List<ImageRequest> images,

        List<LinkRequest> links,

        List<MemberRequest> members
) {

    public record ImageRequest(
            @NotBlank String imageUrl,
            Boolean isMain,
            Integer displayOrder
    ) {
    }

    public record LinkRequest(
            Platform platform,
            @NotBlank String url
    ) {
    }

    public record MemberRequest(
            @NotBlank String name,
            String part
    ) {
    }
}

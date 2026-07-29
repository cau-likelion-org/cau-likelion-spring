package com.example.cau_likelion_spring.organization.dto;

import com.example.cau_likelion_spring.organization.domain.Generation;
import com.example.cau_likelion_spring.organization.domain.Part;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class GenerationListResponseDto {

    private Long id;
    private Integer number;
    private Integer year;
    private Boolean isCurrent;
    private List<PartSummary> parts;

    public static GenerationListResponseDto of(Generation generation, List<Part> parts) {
        return GenerationListResponseDto.builder()
                .id(generation.getId())
                .number(generation.getNumber())
                .year(generation.getYear())
                .isCurrent(generation.getIsCurrent())
                .parts(parts.stream()
                        .map(part -> PartSummary.builder()
                                .id(part.getId())
                                .name(part.getName())
                                .build())
                        .toList())
                .build();
    }

    @Getter
    @Builder
    public static class PartSummary {
        private Long id;
        private String name;
    }
}

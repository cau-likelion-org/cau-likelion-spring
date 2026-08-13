package com.example.cau_likelion_spring.intro.dto;

import com.example.cau_likelion_spring.intro.domain.Roadmap;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RoadmapResponseDto {

    private Long id;
    private String imageUrl;

    public static RoadmapResponseDto from(Roadmap roadmap) {
        return RoadmapResponseDto.builder()
                .id(roadmap.getId())
                .imageUrl(roadmap.getImageUrl())
                .build();
    }
}

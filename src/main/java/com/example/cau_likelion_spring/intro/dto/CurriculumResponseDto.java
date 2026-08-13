package com.example.cau_likelion_spring.intro.dto;

import com.example.cau_likelion_spring.intro.domain.Curriculum;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CurriculumResponseDto {

    private Long id;
    private Long trackId;
    private String trackKoName;
    private String week;
    private String title;
    private String description;

    public static CurriculumResponseDto from(Curriculum curriculum) {
        return CurriculumResponseDto.builder()
                .id(curriculum.getId())
                .trackId(curriculum.getTrack().getId())
                .trackKoName(curriculum.getTrack().getKoName())
                .week(curriculum.getWeek())
                .title(curriculum.getTitle())
                .description(curriculum.getDescription())
                .build();
    }
}

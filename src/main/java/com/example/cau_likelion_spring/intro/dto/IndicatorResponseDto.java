package com.example.cau_likelion_spring.intro.dto;

import com.example.cau_likelion_spring.intro.domain.Indicator;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class IndicatorResponseDto {

    private Long id;
    private String cumulativeGenerations;
    private String cumulativeGraduates;
    private String cumulativeProjects;

    public static IndicatorResponseDto from(Indicator indicator) {
        return IndicatorResponseDto.builder()
                .id(indicator.getId())
                .cumulativeGenerations(indicator.getCumulative_generations())
                .cumulativeGraduates(indicator.getCumulative_graduates())
                .cumulativeProjects(indicator.getCumulative_projects())
                .build();
    }
}

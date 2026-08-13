package com.example.cau_likelion_spring.intro.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 정량 지표 수정 요청 - 항상 전체 필드를 덮어쓴다 (PUT 방식)
 */
@Getter
@NoArgsConstructor
public class IndicatorRequestDto {

    @NotBlank
    private String cumulativeGenerations;

    @NotBlank
    private String cumulativeGraduates;

    @NotBlank
    private String cumulativeProjects;
}

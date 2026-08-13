package com.example.cau_likelion_spring.intro.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 커리큘럼 생성/수정 공통 요청 - 항상 전체 필드를 덮어쓴다 (PUT 방식)
 * trackId는 필수 - 반드시 존재하는 트랙을 가리켜야 한다.
 */
@Getter
@NoArgsConstructor
public class CurriculumRequestDto {

    @NotNull
    private Long trackId;

    @NotNull
    private String week;

    @NotBlank
    private String title;

    @Size(max = 1000)
    private String description;
}

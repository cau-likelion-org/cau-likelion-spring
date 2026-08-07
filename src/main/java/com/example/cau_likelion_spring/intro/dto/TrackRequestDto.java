package com.example.cau_likelion_spring.intro.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 트랙 생성/수정 공통 요청 - 항상 전체 필드를 덮어쓴다 (PUT 방식)
 */
@Getter
@NoArgsConstructor
public class TrackRequestDto {

    @NotBlank
    private String koName;

    @NotBlank
    private String enName;

    @NotBlank
    private String introduction;

    @NotNull
    private List<String> techStack;
}

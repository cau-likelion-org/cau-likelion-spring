package com.example.cau_likelion_spring.intro.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * FAQ 생성/수정 공통 요청 - 항상 전체 필드를 덮어쓴다 (PUT 방식)
 */
@Getter
@NoArgsConstructor
public class FaqRequestDto {

    @NotBlank
    private String question;

    @NotBlank
    @Size(max = 1000)
    private String answer;
}

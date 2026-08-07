package com.example.cau_likelion_spring.intro.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 인재상 생성/수정 공통 요청 - 항상 전체 필드를 덮어쓴다 (PUT 방식)
 * partName이 null이면 공통 인재상, 값이 있으면 해당 파트의 인재상
 */
@Getter
@NoArgsConstructor
public class DesiredTalentRequestDto {

    private String partName;

    @NotBlank
    @Size(max = 1000)
    private String content;
}

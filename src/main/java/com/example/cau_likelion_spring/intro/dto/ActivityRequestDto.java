package com.example.cau_likelion_spring.intro.dto;

import com.example.cau_likelion_spring.intro.domain.PageNavigation;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 활동 생성/수정 공통 요청 - 항상 전체 필드를 덮어쓴다 (PUT 방식)
 * pageNavigation은 선택 항목이라 null 허용
 */
@Getter
@NoArgsConstructor
public class ActivityRequestDto {

    @NotBlank
    private String name;

    @NotBlank
    private String imageUrl;

    @NotBlank
    private String introduction;

    @NotBlank
    private String description;

    @NotBlank
    private String buttonName;

    private PageNavigation pageNavigation;
}

package com.example.cau_likelion_spring.intro.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 로드맵 이미지 추가 요청.
 * imageUrl은 POST /api/files/ROADMAP으로 미리 업로드해 받은 URL을 그대로 담아 보낸다.
 */
@Getter
@NoArgsConstructor
public class RoadmapRequestDto {

    @NotBlank
    private String imageUrl;
}

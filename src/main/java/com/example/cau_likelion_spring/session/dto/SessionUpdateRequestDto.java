package com.example.cau_likelion_spring.session.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/** thumbnailUrl/imageUrls는 POST /api/files/SESSION으로 미리 업로드해 받은 URL을 담는다. 각각 생략하면 기존 값을 유지한다. */
@Getter
@NoArgsConstructor
public class SessionUpdateRequestDto {
    private String title;
    private String description;
    private LocalDateTime sessionDate;
    private Integer degree;
    private String thumbnailUrl;
    private List<String> imageUrls;
}

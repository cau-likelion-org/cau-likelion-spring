package com.example.cau_likelion_spring.session.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

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

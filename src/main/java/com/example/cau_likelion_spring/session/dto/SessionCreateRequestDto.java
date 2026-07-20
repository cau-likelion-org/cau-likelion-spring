package com.example.cau_likelion_spring.session.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class SessionCreateRequestDto {
    @NotBlank
    private String PartName;
    @NotBlank
    private Integer GenerationNumber;

    @NotBlank
    private String title;

    private String description;

    @NotBlank
    private LocalDateTime sessionDate;

    @NotBlank
    private Integer degree;

    @NotBlank
    private String thumbnailUrl;

    private List<String> imageUrls;
}

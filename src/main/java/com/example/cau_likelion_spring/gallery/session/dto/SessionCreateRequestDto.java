package com.example.cau_likelion_spring.gallery.session.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/** thumbnailUrl/imageUrls는 POST /api/files/SESSION으로 미리 업로드해 받은 URL을 담는다. */
@Getter
@NoArgsConstructor
public class SessionCreateRequestDto {

    @NotBlank
    private String partName;

    @NotNull
    private Integer generationNumber;

    @NotBlank
    private String title;

    private String description;

    @NotNull
    private LocalDateTime sessionDate;

    @NotNull
    private Integer degree;

    private String thumbnailUrl;

    private List<String> imageUrls;
}

package com.example.cau_likelion_spring.session.dto;

import com.example.cau_likelion_spring.session.domain.Session;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 세션 상세 조회용 응답
 */
@Getter
@Builder
public class SessionResponseDto {

    private Long id;
    private String partName;
    private Integer generationNumber;
    private Integer degree;
    private String title;
    private String description;
    private LocalDateTime sessionDate;
    private String thumbnailUrl;
    private List<String> imageUrls;

    public static SessionResponseDto from(Session session, List<String> imageUrls) {
        return SessionResponseDto.builder()
                .id(session.getId())
                .partName(session.getPart().getName())
                .generationNumber(session.getPart().getGeneration().getNumber())
                .degree(session.getDegree())
                .title(session.getTitle())
                .description(session.getDescription())
                .sessionDate(session.getSessionDate())
                .thumbnailUrl(session.getThumbnailUrl())
                .imageUrls(imageUrls)
                .build();
    }
}

package com.example.cau_likelion_spring.session.dto;

import com.example.cau_likelion_spring.session.domain.Session;
import lombok.Builder;
import lombok.Getter;

/**
 * 세션 리스트 조회용 응답 (요약 정보만)
 */
@Getter
@Builder
public class SessionListResponseDto {

    private Long id;
    private String title;
    private String thumbnailUrl;
    private String partName;
    private Integer generationNumber;
    private Integer degree;

    public static SessionListResponseDto from(Session session) {
        return SessionListResponseDto.builder()
                .id(session.getId())
                .title(session.getTitle())
                .thumbnailUrl(session.getThumbnailUrl())
                .partName(session.getPart().getName())
                .generationNumber(session.getPart().getGeneration().getNumber())
                .degree(session.getDegree())
                .build();
    }
}

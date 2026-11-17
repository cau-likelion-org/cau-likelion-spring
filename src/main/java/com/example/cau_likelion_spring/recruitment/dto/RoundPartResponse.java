package com.example.cau_likelion_spring.recruitment.dto;

import com.example.cau_likelion_spring.recruitment.domain.RoundPart;
import lombok.Builder;
import lombok.Getter;

/**
 * 지원 파트 목록 응답 (7.4.1 지원 파트 드롭다운).
 * 지원자는 id를 골라 문항 조회·제출 API에 보낸다.
 */
@Getter
@Builder
public class RoundPartResponse {

    private Long id;
    private String part;

    public static RoundPartResponse from(RoundPart roundPart) {
        return RoundPartResponse.builder()
                .id(roundPart.getId())
                .part(roundPart.getPart())
                .build();
    }
}

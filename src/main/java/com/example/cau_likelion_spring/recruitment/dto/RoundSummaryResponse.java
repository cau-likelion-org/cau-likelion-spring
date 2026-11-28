package com.example.cau_likelion_spring.recruitment.dto;

import com.example.cau_likelion_spring.recruitment.domain.Round;
import io.swagger.v3.oas.annotations.media.Schema;

/** 회차 목록 항목 (대시보드 회차 선택 드롭다운용) */
@Schema(description = "회차 목록 항목")
public record RoundSummaryResponse(

        @Schema(description = "회차 id", example = "1")
        Long id,

        @Schema(description = "회차명", example = "14기")
        String name
) {
    public static RoundSummaryResponse from(Round round) {
        return new RoundSummaryResponse(round.getId(), round.getName());
    }
}

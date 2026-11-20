package com.example.cau_likelion_spring.recruitment.dto;

import com.example.cau_likelion_spring.recruitment.domain.Round;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "모집공고 응답")
public record RoundNoticeResponse(

        @Schema(description = "모집 회차 id (수정 API 경로에 사용)")
        Long roundId,

        @Schema(description = "서류접수 시작일시")
        LocalDateTime startDate,

        @Schema(description = "서류접수 마감일시")
        LocalDateTime deadLine,

        @Schema(description = "서류 결과발표 일시")
        LocalDateTime documentResultDate,

        @Schema(description = "OT 일시")
        LocalDateTime otDate
) {
    public static RoundNoticeResponse from(Round round) {
        return new RoundNoticeResponse(
                round.getId(),
                round.getStartDate(),
                round.getDeadLine(),
                round.getDocumentResultDate(),
                round.getOtDate()
        );
    }
}

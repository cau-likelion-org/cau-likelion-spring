package com.example.cau_likelion_spring.recruitment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * 모집공고 등록/수정 요청 - 수정 시에도 항상 전체 일정을 덮어쓴다 (PUT 방식).
 * 일정은 서류접수 시작 < 서류접수 마감 < 서류 결과발표 < OT 순서여야 한다.
 */
@Schema(description = "모집공고 등록/수정 요청")
public record RoundNoticeRequest(

        @Schema(description = "서류접수 시작일시", example = "2027-03-02T10:00:00")
        @NotNull LocalDateTime startDate,

        @Schema(description = "서류접수 마감일시", example = "2027-03-09T23:59:00")
        @NotNull LocalDateTime deadLine,

        @Schema(description = "서류 결과발표 일시", example = "2027-03-12T18:00:00")
        @NotNull LocalDateTime documentResultDate,

        @Schema(description = "OT 일시", example = "2027-03-20T14:00:00")
        @NotNull LocalDateTime otDate
) {
}

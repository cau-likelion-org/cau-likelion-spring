package com.example.cau_likelion_spring.recruitment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * 백오피스 최종 결과 공개 예약 요청.
 * 일시 간 순서(서류 공개 < 최종 공개 < 결과 확인 종료 ≤ 서류 모집 마감 + 3개월)는 서비스에서 검증한다.
 */
@Schema(description = "최종 결과 공개 예약 요청")
public record FinalPublishReserveRequest(

        @Schema(description = "공개 일시 (현재 이후, 서류 결과 공개 일시 이후)", example = "2027-03-20T18:00:00")
        @NotNull LocalDateTime publishAt,

        @Schema(description = "결과 확인 종료일 (공개 일시 이후, 서류 모집 마감일 + 3개월 이내)", example = "2027-05-31T23:59:00")
        @NotNull LocalDateTime resultCheckDeadLine
) {
}

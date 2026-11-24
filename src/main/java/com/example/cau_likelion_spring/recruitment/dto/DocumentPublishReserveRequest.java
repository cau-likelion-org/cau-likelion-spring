package com.example.cau_likelion_spring.recruitment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/** 백오피스 서류 결과 공개 예약 요청 */
@Schema(description = "서류 결과 공개 예약 요청")
public record DocumentPublishReserveRequest(

        @Schema(description = "공개 일시 (현재 이후)", example = "2027-03-10T18:00:00")
        @NotNull LocalDateTime publishAt
) {
}

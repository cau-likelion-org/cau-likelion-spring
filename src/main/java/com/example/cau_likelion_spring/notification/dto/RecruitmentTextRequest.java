package com.example.cau_likelion_spring.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "모집 공고 작성/예약 요청")
public record RecruitmentTextRequest(

        @Schema(description = "제목")
        @NotBlank String title,

        @Schema(description = "본문 내용")
        @NotBlank String content,

        @Schema(description = "예정 전송일시")
        @NotNull LocalDateTime scheduledSendAt,

        @Schema(description = "발송 대상 구독자 ID 목록")
        @NotEmpty List<Long> subscriberIds
) {
}

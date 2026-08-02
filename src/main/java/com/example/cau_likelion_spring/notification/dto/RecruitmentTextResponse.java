package com.example.cau_likelion_spring.notification.dto;

import com.example.cau_likelion_spring.notification.domain.RecruitmentText;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "모집 공고 응답")
public record RecruitmentTextResponse(

        @Schema(description = "공고 ID", example = "1")
        Long id,

        @Schema(description = "제목")
        String title,

        @Schema(description = "본문 내용")
        String content,

        @Schema(description = "예정 전송일시")
        LocalDateTime scheduledSendAt,

        @Schema(description = "작성일시")
        LocalDateTime createdAt,

        @Schema(description = "발송 대상자 수", example = "42")
        int targetCount
) {

    public static RecruitmentTextResponse of(RecruitmentText text, int targetCount) {
        return new RecruitmentTextResponse(
                text.getId(),
                text.getTitle(),
                text.getContent(),
                text.getScheduledSendAt(),
                text.getCreatedAt(),
                targetCount
        );
    }
}

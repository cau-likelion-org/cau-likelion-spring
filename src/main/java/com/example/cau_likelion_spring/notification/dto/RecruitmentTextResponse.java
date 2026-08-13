package com.example.cau_likelion_spring.notification.dto;

import com.example.cau_likelion_spring.notification.domain.RecruitmentSendStatus;
import com.example.cau_likelion_spring.notification.domain.RecruitmentText;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "모집 공고 응답")
public record RecruitmentTextResponse(

        @Schema(description = "공고 ID", example = "1")
        Long id,

        @Schema(description = "제목")
        String title,

        @Schema(description = "본문 내용")
        String content,

        @Schema(description = "예정 전송일시. 취소된 공고는 더 이상 발송되지 않으므로 null")
        LocalDateTime scheduledSendAt,

        @Schema(description = "작성일시")
        LocalDateTime createdAt,

        @Schema(description = "발송 대상자 수", example = "42")
        int targetCount,

        @Schema(description = "발송 성공 건수", example = "40")
        int successCount,

        @Schema(description = "발송 실패 건수", example = "2")
        int failedCount,

        @Schema(description = "수신자별 발송 결과 (구독자가 삭제된 대상은 제외)")
        List<RecipientResponse> recipients,

        @Schema(description = "공고 발송 상태 (수신자별 성공/실패가 아닌, 공고 자체의 발송 진행 상태)")
        RecruitmentSendStatus status
) {

    public static RecruitmentTextResponse of(
            RecruitmentText text, int targetCount, int successCount, int failedCount, List<RecipientResponse> recipients) {
        boolean cancelled = text.getStatus() == RecruitmentSendStatus.CANCELLED;
        return new RecruitmentTextResponse(
                text.getId(),
                text.getTitle(),
                text.getContent(),
                cancelled ? null : text.getScheduledSendAt(),
                text.getCreatedAt(),
                targetCount,
                successCount,
                failedCount,
                recipients,
                text.getStatus()
        );
    }
}

package com.example.cau_likelion_spring.notification.dto;

import com.example.cau_likelion_spring.notification.domain.EmailSentLog;
import com.example.cau_likelion_spring.notification.domain.EmailSentStatus;
import com.example.cau_likelion_spring.notification.domain.RecruitmentSubscriber;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "이메일 발송 이력 응답")
public record EmailSentLogResponse(

        @Schema(description = "발송 로그 ID", example = "1")
        Long id,

        @Schema(description = "구독자 ID (구독자가 삭제된 경우 null)", example = "1")
        Long subscriberId,

        @Schema(description = "구독자 이메일 (구독자가 삭제된 경우 null)")
        String subscriberEmail,

        @Schema(description = "발송(예정)된 제목 (공고 원본 제목)")
        String title,

        @Schema(description = "발송(예정)된 본문 (공고 원본 본문)")
        String content,

        @Schema(description = "발송 상태")
        EmailSentStatus status,

        @Schema(description = "발송 시각 (PENDING인 경우 null)")
        LocalDateTime sentAt
) {

    public static EmailSentLogResponse of(EmailSentLog log) {
        RecruitmentSubscriber subscriber = log.getSubscriber();
        return new EmailSentLogResponse(
                log.getId(),
                subscriber != null ? subscriber.getId() : null,
                subscriber != null ? subscriber.getEmail() : null,
                log.getRecruitmentText().getTitle(),
                log.getRecruitmentText().getContent(),
                log.getStatus(),
                log.getSentAt()
        );
    }
}

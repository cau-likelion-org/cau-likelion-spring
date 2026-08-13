package com.example.cau_likelion_spring.notification.dto;

import com.example.cau_likelion_spring.notification.domain.EmailSentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "수신자별 발송 결과")
public record RecipientResponse(

        @Schema(description = "수신자 이메일")
        String email,

        @Schema(description = "발송 상태")
        EmailSentStatus status
) {
}

package com.example.cau_likelion_spring.recruitment.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/** 인증번호 발송 응답 */
@Getter
@Builder
public class EmailVerificationSendResponse {

    /** 인증번호 만료 시각 (화면의 남은 시간 표시용) */
    private LocalDateTime expiresAt;

    public static EmailVerificationSendResponse of(LocalDateTime expiresAt) {
        return EmailVerificationSendResponse.builder()
                .expiresAt(expiresAt)
                .build();
    }
}

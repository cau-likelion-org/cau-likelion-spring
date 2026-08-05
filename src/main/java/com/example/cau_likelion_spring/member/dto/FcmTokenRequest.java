package com.example.cau_likelion_spring.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "FCM 토큰 등록/갱신 요청")
public record FcmTokenRequest(

        @Schema(description = "PWA 클라이언트가 발급받은 FCM 토큰")
        @NotBlank String fcmToken
) {
}

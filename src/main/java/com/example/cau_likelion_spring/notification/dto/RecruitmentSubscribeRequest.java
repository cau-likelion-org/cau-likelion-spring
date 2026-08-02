package com.example.cau_likelion_spring.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "모집 알림 구독 신청 요청")
public record RecruitmentSubscribeRequest(

        @Schema(description = "구독 이메일", example = "example@likelion.org")
        @NotBlank @Email String email
) {
}

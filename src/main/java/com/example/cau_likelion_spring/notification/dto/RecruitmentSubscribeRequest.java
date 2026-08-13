package com.example.cau_likelion_spring.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Schema(description = "모집 알림 구독 신청 요청")
public record RecruitmentSubscribeRequest(

        @Schema(description = "구독 이메일", example = "example@likelion.org")
        @NotBlank @Email String email,

        @Schema(description = "신청자 이름", example = "홍길동")
        @NotBlank String name,

        @Schema(description = "관심 파트 id 목록, 1개 이상 (GET /api/recruitment/subscribers/available-parts 조회 결과 중 선택)", example = "[6, 7]")
        @NotEmpty List<Long> interestPartIds
) {
}

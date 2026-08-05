package com.example.cau_likelion_spring.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "실패 건 재전송 요청. 생략하거나 필드를 비워두면 원본 공고의 제목/본문으로 재전송한다.")
public record EmailResendRequest(

        @Schema(description = "재전송 시 사용할 제목 (미입력 시 원본 공고 제목 사용)")
        String title,

        @Schema(description = "재전송 시 사용할 본문 (미입력 시 원본 공고 본문 사용)")
        String content
) {
}

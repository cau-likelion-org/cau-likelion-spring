package com.example.cau_likelion_spring.member.dto;

import com.example.cau_likelion_spring.member.domain.AllowedUserEmail;
import io.swagger.v3.oas.annotations.media.Schema;

public record AllowedUserEmailResponse(

        @Schema(description = "예비 회원 id")
        Long id,

        @Schema(description = "이름")
        String name,

        @Schema(description = "가입 예정 이메일")
        String email
) {

    public static AllowedUserEmailResponse from(AllowedUserEmail entity) {
        return new AllowedUserEmailResponse(entity.getId(), entity.getName(), entity.getAllowedEmail());
    }
}

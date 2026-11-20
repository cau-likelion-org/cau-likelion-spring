package com.example.cau_likelion_spring.recruitment.dto;

import lombok.Builder;
import lombok.Getter;

/** 인증번호 확인 응답 */
@Getter
@Builder
public class EmailVerificationConfirmResponse {

    /** 인증 완료 토큰. 계정 생성·비밀번호 재설정 요청에 함께 보낸다 */
    private String verificationToken;

    public static EmailVerificationConfirmResponse of(String verificationToken) {
        return EmailVerificationConfirmResponse.builder()
                .verificationToken(verificationToken)
                .build();
    }
}

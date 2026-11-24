package com.example.cau_likelion_spring.recruitment.dto;

import lombok.Builder;
import lombok.Getter;

/** 지원자 계정 생성 응답 */
@Getter
@Builder
public class ApplicantSignupResponse {

    /** 지원자 토큰. 이후 지원자 API 호출 시 Authorization 헤더에 Bearer로 보낸다 */
    private String accessToken;

    public static ApplicantSignupResponse of(String accessToken) {
        return ApplicantSignupResponse.builder()
                .accessToken(accessToken)
                .build();
    }
}

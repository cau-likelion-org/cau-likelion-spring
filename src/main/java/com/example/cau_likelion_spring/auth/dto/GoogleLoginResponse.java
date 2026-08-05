package com.example.cau_likelion_spring.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * status로 두 상황을 구분한다.
 * - LOGIN_SUCCESS: tokens에 값이 들어있고, signupToken은 null
 * - SIGNUP_REQUIRED: signupToken에 값이 들어있고, tokens는 null (프론트는 이 값을 들고 회원가입 폼으로 이동)
 */
public record GoogleLoginResponse(

        @Schema(description = "로그인 처리 결과")
        Status status,

        @Schema(description = "status가 LOGIN_SUCCESS일 때만 값이 있음")
        TokenResponse tokens,

        @Schema(description = "status가 SIGNUP_REQUIRED일 때만 값이 있음. POST /api/auth/join 호출 시 그대로 사용")
        String signupToken
) {

    public enum Status {
        LOGIN_SUCCESS,
        SIGNUP_REQUIRED
    }

    public static GoogleLoginResponse loginSuccess(TokenResponse tokens) {
        return new GoogleLoginResponse(Status.LOGIN_SUCCESS, tokens, null);
    }

    public static GoogleLoginResponse signupRequired(String signupToken) {
        return new GoogleLoginResponse(Status.SIGNUP_REQUIRED, null, signupToken);
    }
}

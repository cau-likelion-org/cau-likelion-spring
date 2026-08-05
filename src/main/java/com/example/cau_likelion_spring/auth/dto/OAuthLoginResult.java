package com.example.cau_likelion_spring.auth.dto;

/**
 * 구글 로그인 콜백 처리 결과. OAuth2LoginSuccessHandler가 이 결과에 따라
 * 프론트엔드의 서로 다른 화면(로그인 완료 / 회원가입 폼 / 에러)으로 리다이렉트한다.
 */
public sealed interface OAuthLoginResult {

    record LoginSuccess(TokenResponse tokens) implements OAuthLoginResult {
    }

    record SignupRequired(String signupToken) implements OAuthLoginResult {
    }
}

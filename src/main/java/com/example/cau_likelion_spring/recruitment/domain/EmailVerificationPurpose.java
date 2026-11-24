package com.example.cau_likelion_spring.recruitment.domain;

/** 이메일 인증 용도. 같은 이메일이라도 용도가 다르면 인증번호를 따로 관리한다 */
public enum EmailVerificationPurpose {
    /** 0단계 계정 생성 */
    SIGNUP,
    /** 비밀번호 재설정 */
    PASSWORD_RESET
}

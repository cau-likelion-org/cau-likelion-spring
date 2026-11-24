package com.example.cau_likelion_spring.recruitment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 지원자 비밀번호 재설정 요청. 대상 이메일은 인증 완료 토큰에 담긴 값을 쓴다 */
@Getter
@NoArgsConstructor
public class ApplicantPasswordResetRequest {

    /** 인증번호 확인 API가 돌려준 인증 완료 토큰 (용도 PASSWORD_RESET) */
    @NotBlank
    private String verificationToken;

    /** 가입과 같은 규칙: 영문 대소문자·숫자·특수문자, 8~64자 */
    @NotBlank
    @Pattern(regexp = "^[A-Za-z\\d\\p{Punct}]{8,64}$", message = "비밀번호가 형식에 맞지 않습니다.")
    private String newPassword;
}

package com.example.cau_likelion_spring.recruitment.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 0단계 지원자 계정 생성 요청 */
@Getter
@NoArgsConstructor
public class ApplicantSignupRequest {

    @NotBlank
    @Size(max = 10)
    private String name;

    /** 하이픈 없이 숫자만 (예: 01012345678) */
    @NotBlank
    @Pattern(regexp = "^01\\d{8,9}$", message = "전화번호는 하이픈 없이 숫자 10~11자리여야 합니다.")
    private String phone;

    @NotBlank
    @Email
    @Size(max = 100)
    private String email;

    /** 영문 대소문자·숫자·특수문자, 8~64자 (BCrypt는 72바이트까지만 반영하므로 상한을 둔다) */
    @NotBlank
    @Pattern(regexp = "^[A-Za-z\\d\\p{Punct}]{8,64}$",
            message = "비밀번호는 영문 대소문자, 숫자, 특수문자로 8자 이상 64자 이하여야 합니다.")
    private String password;

    /** 개인정보 수집·이용 동의. 동의해야만 가입되고 값은 따로 저장하지 않는다 */
    @AssertTrue(message = "개인정보 수집·이용에 동의해야 합니다.")
    private boolean privacyAgreed;

    /** 인증번호 확인 API가 돌려준 인증 완료 토큰 (용도 SIGNUP) */
    @NotBlank
    private String verificationToken;
}

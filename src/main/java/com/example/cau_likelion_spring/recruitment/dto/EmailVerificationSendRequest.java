package com.example.cau_likelion_spring.recruitment.dto;

import com.example.cau_likelion_spring.recruitment.domain.EmailVerificationPurpose;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 인증번호 발송 요청 */
@Getter
@NoArgsConstructor
public class EmailVerificationSendRequest {

    @NotBlank
    @Email
    @Size(max = 100)
    private String email;

    /** SIGNUP(계정 생성) 또는 PASSWORD_RESET(비밀번호 재설정) */
    @NotNull
    private EmailVerificationPurpose purpose;
}

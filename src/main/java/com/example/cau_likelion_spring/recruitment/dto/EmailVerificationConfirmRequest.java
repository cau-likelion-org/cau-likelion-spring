package com.example.cau_likelion_spring.recruitment.dto;

import com.example.cau_likelion_spring.recruitment.domain.EmailVerificationPurpose;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 인증번호 확인 요청. email·purpose는 발송 요청과 같은 값을 보낸다 */
@Getter
@NoArgsConstructor
public class EmailVerificationConfirmRequest {

    @NotBlank
    @Email
    @Size(max = 100)
    private String email;

    @NotNull
    private EmailVerificationPurpose purpose;

    @NotBlank
    @Pattern(regexp = "^\\d{6}$", message = "인증번호는 6자리 숫자여야 합니다.")
    private String code;
}

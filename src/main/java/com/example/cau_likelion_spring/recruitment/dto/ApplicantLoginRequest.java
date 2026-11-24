package com.example.cau_likelion_spring.recruitment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 지원자 로그인 요청 */
@Getter
@NoArgsConstructor
public class ApplicantLoginRequest {

    @NotBlank
    private String email;

    /** 형식은 검사하지 않는다 (형식이 틀리면 불일치로 401) */
    @NotBlank
    private String password;
}

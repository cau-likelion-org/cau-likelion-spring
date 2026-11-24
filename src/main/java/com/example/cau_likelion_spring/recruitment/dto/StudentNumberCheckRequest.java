package com.example.cau_likelion_spring.recruitment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 학번 중복 조회 요청.
 * 학번이 접근 로그(URL)에 남지 않도록 쿼리스트링 대신 요청 바디로 받는다.
 */
@Getter
@NoArgsConstructor
public class StudentNumberCheckRequest {

    @NotBlank
    @Pattern(regexp = "^\\d{8}$", message = "학번은 8자리 숫자여야 합니다.")
    private String studentNumber;
}

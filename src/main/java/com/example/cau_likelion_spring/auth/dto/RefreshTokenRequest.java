package com.example.cau_likelion_spring.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(

        @NotBlank(message = "refreshToken은 필수입니다.")
        String refreshToken
) {
}

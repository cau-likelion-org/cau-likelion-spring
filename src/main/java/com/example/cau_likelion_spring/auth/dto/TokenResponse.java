package com.example.cau_likelion_spring.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record TokenResponse(

        @Schema(description = "Access Token")
        String accessToken,

        @Schema(description = "Refresh Token")
        String refreshToken
) {
}

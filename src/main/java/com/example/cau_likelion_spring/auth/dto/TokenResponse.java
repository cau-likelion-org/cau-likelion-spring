package com.example.cau_likelion_spring.auth.dto;

public record TokenResponse(
        String accessToken,
        String refreshToken
) {
}

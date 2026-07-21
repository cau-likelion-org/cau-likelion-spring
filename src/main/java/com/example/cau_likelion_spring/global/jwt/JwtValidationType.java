package com.example.cau_likelion_spring.global.jwt;

public enum JwtValidationType {
    VALID_JWT,
    INVALID_JWT_SIGNATURE,
    INVALID_JWT_TOKEN,
    EXPIRED_JWT_TOKEN,
    UNSUPPORTED_JWT_TOKEN,
    EMPTY_JWT
}

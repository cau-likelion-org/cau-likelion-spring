package com.example.cau_likelion_spring.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequest(

        @Schema(description = "구글 로그인(Google Identity Services) 성공 시 프론트가 받는 ID Token", example = "eyJhbGciOiJSUzI1NiJ9...")
        @NotBlank(message = "idToken은 필수입니다.")
        String idToken
) {
}

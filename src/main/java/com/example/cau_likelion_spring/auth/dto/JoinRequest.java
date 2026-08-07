package com.example.cau_likelion_spring.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record JoinRequest(

        @Schema(description = "구글 로그인 성공 시 발급되는 가입용 임시 토큰", example = "eyJhbGciOiJIUzI1NiJ9...")
        @NotBlank(message = "가입 토큰은 필수입니다.")
        String signupToken,

        @Schema(description = "이름", example = "홍길동")
        @NotBlank(message = "이름은 필수입니다.")
        String name,

        @Schema(description = "기수 ID", example = "1")
        @NotNull(message = "기수는 필수입니다.")
        Long generationId,

        @Schema(description = "파트 ID", example = "1")
        @NotNull(message = "파트는 필수입니다.")
        Long partId
) {
}

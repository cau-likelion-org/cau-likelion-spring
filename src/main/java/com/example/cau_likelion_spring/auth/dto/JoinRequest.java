package com.example.cau_likelion_spring.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record JoinRequest(

        @Schema(description = "이메일 (사전 등록된 이메일이어야 함)", example = "test@example.com")
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "이메일 형식이 맞지 않습니다.")
        String email,

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

package com.example.cau_likelion_spring.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record JoinRequest(

        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "이메일 형식이 맞지 않습니다.")
        String email,

        @NotBlank(message = "이름은 필수입니다.")
        String name,

        @NotNull(message = "기수는 필수입니다.")
        Long generationId,

        @NotNull(message = "파트는 필수입니다.")
        Long partId
) {
}

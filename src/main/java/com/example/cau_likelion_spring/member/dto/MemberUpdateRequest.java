package com.example.cau_likelion_spring.member.dto;

import com.example.cau_likelion_spring.member.domain.MemberRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "구성원 정보 수정 요청")
public record MemberUpdateRequest(

        @Schema(description = "이름", example = "홍길동")
        @NotBlank String name,

        @Schema(description = "로그인 이메일", example = "hong@example.com")
        @NotBlank String email,

        @Schema(description = "역할")
        @NotNull MemberRole role,

        @Schema(description = "파트 ID (소속 파트가 없으면 null). 기수는 파트에 종속되므로 파트를 바꾸면 기수도 함께 바뀐다", example = "1")
        Long partId
) {
}

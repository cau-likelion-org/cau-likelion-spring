package com.example.cau_likelion_spring.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 예비 회원 관리 화면에서 "저장" 클릭 시 현재 화면에 보이는 목록 전체를 그대로 담아 보내는 요청.
 * id가 없으면 신규 생성, 있으면 수정. 기존에 있었지만 이 목록에 없는 항목은 삭제된다.
 */
public record AllowedUserEmailSyncRequest(

        @NotNull(message = "items는 필수입니다.")
        @Valid
        List<AllowedUserEmailItem> items
) {

    public record AllowedUserEmailItem(

            @Schema(description = "기존 항목의 id. 신규 추가된 행이면 null", example = "1")
            Long id,

            @Schema(description = "이름", example = "홍길동")
            @NotBlank(message = "이름은 필수입니다.")
            String name,

            @Schema(description = "가입 예정 이메일", example = "test@gmail.com")
            @NotBlank(message = "이메일은 필수입니다.")
            @Email(message = "이메일 형식이 맞지 않습니다.")
            String email
    ) {
    }
}

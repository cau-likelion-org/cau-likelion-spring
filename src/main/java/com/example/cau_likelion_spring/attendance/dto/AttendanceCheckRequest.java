package com.example.cau_likelion_spring.attendance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;

@Schema(description = "출석체크 요청")
public record AttendanceCheckRequest(

        @Schema(description = "출석 체크용 비밀번호 (4자리 숫자)", example = "0091")
        @Pattern(regexp = "\\d{4}", message = "비밀번호는 4자리 숫자여야 합니다.")
        String password
) {
}

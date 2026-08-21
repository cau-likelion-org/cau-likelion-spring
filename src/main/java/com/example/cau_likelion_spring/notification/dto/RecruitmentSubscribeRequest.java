package com.example.cau_likelion_spring.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

import java.util.List;

@Schema(description = "모집 알림 구독 신청 요청")
public record RecruitmentSubscribeRequest(

        // @Email만으로는 한글 자모 등 유니코드 문자가 섞인 로컬파트(예: "sdfㄴㄴ@naver.com")를 걸러내지 못해 ASCII로 제한하는 @Pattern을 추가로 검증한다.
        @Schema(description = "구독 이메일", example = "example@likelion.org")
        @NotBlank @Email
        @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "이메일 형식이 맞지 않습니다.")
        String email,

        @Schema(description = "신청자 이름", example = "홍길동")
        @NotBlank String name,

        @Schema(description = "관심 파트 id 목록, 1개 이상 (GET /api/recruitment/subscribers/available-parts 조회 결과 중 선택)", example = "[6, 7]")
        @NotEmpty List<Long> interestPartIds
) {
}

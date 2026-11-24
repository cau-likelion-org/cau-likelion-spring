package com.example.cau_likelion_spring.recruitment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 백오피스 결과 안내 문구 저장 요청.
 * 면접 변수를 서류 합격 외 템플릿에 넣었는지는 서비스에서 검증한다.
 */
@Schema(description = "결과 안내 문구 저장 요청")
public record ResultTemplateSaveRequest(

        @Schema(description = "문구. 변수는 버튼으로 삽입한 토큰({이름}, {면접 일시}, {면접 대기 장소}) 그대로 보냅니다.",
                example = "{이름}님, 서류 전형에 합격하셨습니다.\n면접 일시: {면접 일시}\n면접 20분 전까지 대기 장소에 도착해주세요.")
        @NotBlank @Size(max = 2000) String text
) {
}

package com.example.cau_likelion_spring.recruitment.dto;

import com.example.cau_likelion_spring.recruitment.domain.QuestionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 백오피스 2단계 문항 등록·수정 요청 (문항 하나).
 * 수정도 부분 수정이 아니라 이 문항의 내용을 통째로 덮어쓴다 (유형 변경 가능, 선택지는 보낸 목록으로 교체).
 * 유형별 규칙(장문은 charLimit 필수, 객관식 단일·복수는 선택지 2개 이상, 링크·파일은 둘 다 없음)은 서비스에서 검증한다.
 */
@Schema(description = "백오피스 2단계 문항 등록·수정 요청")
public record QuestionSaveRequest(

        @Schema(description = "문항 유형 (LONG_TEXT: 장문, LINK: 링크, FILE: 파일(PDF 1개, 30MB 이하), "
                + "SINGLE_CHOICE: 객관식 단일, MULTIPLE_CHOICE: 객관식 복수)", example = "LONG_TEXT")
        @NotNull QuestionType type,

        @Schema(description = "질문 (필수 입력)", example = "지원 동기를 작성해주세요.")
        @NotBlank @Size(max = 1000) String question,

        @Schema(description = "설명글 (선택)", example = "구체적인 경험을 바탕으로 작성해주세요.")
        @Size(max = 1000) String detail,

        @Schema(description = "지원자 필수 응답 여부 (토글)", example = "true")
        @NotNull Boolean required,

        @Schema(description = "글자 수 제한 (공백 포함). 장문만 필수, 나머지 유형은 보내지 않음", example = "500")
        @Positive Integer charLimit,

        @Schema(description = "선택지 목록 (배열 순서 = 노출 순서). 객관식 단일·복수만 2개 이상 필수, 나머지 유형은 보내지 않음")
        List<@NotBlank @Size(max = 100) String> options
) {
}

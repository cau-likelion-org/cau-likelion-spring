package com.example.cau_likelion_spring.recruitment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 모집공고 FAQ 저장 요청 - 회차의 FAQ 목록을 통째로 덮어쓴다.
 * 배열 순서가 곧 노출 순서이며, FAQ는 최소 1개 있어야 한다.
 */
@Schema(description = "모집공고 FAQ 저장 요청 (목록 전체 덮어쓰기)")
public record RoundFaqRequest(

        @Schema(description = "FAQ 목록 (배열 순서 = 노출 순서)")
        @NotEmpty(message = "FAQ는 최소 1개 이상 있어야 합니다.")
        List<@Valid @NotNull FaqItem> faqs
) {
    public record FaqItem(

            @Schema(description = "질문", example = "비전공자도 지원할 수 있나요?")
            @NotBlank @Size(max = 1000) String question,

            @Schema(description = "답변", example = "네, 전공과 무관하게 지원할 수 있습니다.")
            @NotBlank @Size(max = 1000) String answer
    ) {
    }
}

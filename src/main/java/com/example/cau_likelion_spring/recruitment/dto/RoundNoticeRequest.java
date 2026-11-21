package com.example.cau_likelion_spring.recruitment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 모집공고 등록/수정 요청 - 일정과 FAQ 목록을 한 번에 저장한다.
 * 수정 시에도 항상 전체를 덮어쓴다 (PUT 방식, FAQ 목록도 통째로 교체).
 * 일정은 서류접수 시작 < 서류접수 마감 < 서류 결과발표 < OT 순서여야 하고, FAQ는 최소 1개 있어야 한다.
 */
@Schema(description = "모집공고 등록/수정 요청 (일정 + FAQ)")
public record RoundNoticeRequest(

        @Schema(description = "서류접수 시작일시", example = "2027-03-02T10:00:00")
        @NotNull LocalDateTime startDate,

        @Schema(description = "서류접수 마감일시", example = "2027-03-09T23:59:00")
        @NotNull LocalDateTime deadLine,

        @Schema(description = "서류 결과발표 일시", example = "2027-03-12T18:00:00")
        @NotNull LocalDateTime documentResultDate,

        @Schema(description = "OT 일시", example = "2027-03-20T14:00:00")
        @NotNull LocalDateTime otDate,

        @Schema(description = "FAQ 목록 (배열 순서 = 노출 순서, 최소 1개)")
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

package com.example.cau_likelion_spring.recruitment.dto;

import com.example.cau_likelion_spring.recruitment.domain.FAQ;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "모집공고 FAQ 응답")
public record RoundFaqResponse(
        Long id,
        String question,
        String answer,
        @Schema(description = "노출 순서 (1부터)") Integer displayOrder
) {
    public static RoundFaqResponse from(FAQ faq) {
        return new RoundFaqResponse(faq.getId(), faq.getQuestion(), faq.getAnswer(), faq.getDisplayOrder());
    }
}

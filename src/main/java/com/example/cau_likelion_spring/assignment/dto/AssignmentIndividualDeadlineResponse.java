package com.example.cau_likelion_spring.assignment.dto;

import com.example.cau_likelion_spring.assignment.domain.AssignmentIndividualDeadline;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "적용된 개별 마감일")
public record AssignmentIndividualDeadlineResponse(

        @Schema(description = "멤버 ID", example = "1")
        Long memberId,

        @Schema(description = "멤버 이름")
        String memberName,

        @Schema(description = "적용된 개별 마감일")
        LocalDateTime deadline
) {

    public static AssignmentIndividualDeadlineResponse of(AssignmentIndividualDeadline individualDeadline) {
        return new AssignmentIndividualDeadlineResponse(
                individualDeadline.getMember().getId(),
                individualDeadline.getMember().getName(),
                individualDeadline.getDeadline()
        );
    }
}

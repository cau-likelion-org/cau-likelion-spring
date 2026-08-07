package com.example.cau_likelion_spring.assignment.dto;

import com.example.cau_likelion_spring.assignment.domain.Assignment;
import com.example.cau_likelion_spring.assignment.domain.AssignmentType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "과제 응답")
public record AssignmentResponse(

        @Schema(description = "과제 ID", example = "1")
        Long id,

        @Schema(description = "파트 ID", example = "1")
        Long partId,

        @Schema(description = "파트 이름")
        String partName,

        @Schema(description = "주차", example = "1")
        Integer week,

        @Schema(description = "과제명")
        String title,

        @Schema(description = "과제 설명")
        String detail,

        @Schema(description = "마감 기한")
        LocalDateTime endDate,

        @Schema(description = "제출 형식")
        AssignmentType type,

        @Schema(description = "생성일시")
        LocalDateTime createdAt
) {

    public static AssignmentResponse of(Assignment assignment) {
        return new AssignmentResponse(
                assignment.getId(),
                assignment.getPart().getId(),
                assignment.getPart().getName(),
                assignment.getWeek(),
                assignment.getTitle(),
                assignment.getDetail(),
                assignment.getEndDate(),
                assignment.getType(),
                assignment.getCreatedAt()
        );
    }
}

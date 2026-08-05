package com.example.cau_likelion_spring.assignment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "개별 마감일 변경 요청 (선택한 아기사자 전원에게 동일한 마감일 적용)")
public record AssignmentIndividualDeadlineRequest(

        @Schema(description = "마감일을 변경할 아기사자 멤버 ID 목록 (다중 선택 가능)")
        @NotEmpty List<Long> memberIds,

        @Schema(description = "새 마감일")
        @NotNull LocalDateTime deadline
) {
}

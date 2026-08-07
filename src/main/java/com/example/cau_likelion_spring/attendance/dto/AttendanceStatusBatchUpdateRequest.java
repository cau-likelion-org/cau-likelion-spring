package com.example.cau_likelion_spring.attendance.dto;

import com.example.cau_likelion_spring.attendance.domain.AttendanceStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "출결 상태 일괄 수정 요청")
public record AttendanceStatusBatchUpdateRequest(

        @Schema(description = "수정할 출결 상태 목록")
        @NotEmpty
        List<@Valid Item> updates
) {

    @Schema(description = "출결 상태 수정 항목")
    public record Item(

            @Schema(description = "출결 상세 기록 ID")
            @NotNull Long detailAttendanceId,

            @Schema(description = "변경할 출결 상태")
            @NotNull AttendanceStatus status,

            @Schema(description = "결석/공결 사유 (결석·공결로 변경 시 필수)")
            String reason
    ) {
    }
}

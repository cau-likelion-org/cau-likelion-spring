package com.example.cau_likelion_spring.recruitment.dto;

import com.example.cau_likelion_spring.recruitment.domain.ResultPublishStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/** 백오피스 서류 결과 공개 예약 현황 (상태·예약 일시·공개 조건 충족 여부) */
@Schema(description = "서류 결과 공개 예약 현황")
public record ResultPublishResponse(

        @Schema(description = "상태 (INPUTTING: 입력 중, RESERVED: 공개 예약, PUBLISHED: 공개)", example = "INPUTTING")
        ResultPublishStatus status,

        @Schema(description = "공개 예약 일시 (입력 중이면 null)", example = "2027-03-10T18:00:00")
        LocalDateTime publishAt,

        @Schema(description = "[예약] 버튼 활성 여부 (입력 중이고 조건을 모두 만족하면 true)", example = "false")
        boolean reservable,

        @Schema(description = "[예약 취소] 버튼 활성 여부 (공개 예약 상태면 true)", example = "false")
        boolean cancellable,

        @Schema(description = "공개 조건 목록")
        List<Condition> conditions
) {

    @Schema(description = "공개 조건")
    public record Condition(

            @Schema(description = "조건 종류", example = "DOCUMENT_RESULT_ENTERED")
            ResultPublishCondition type,

            @Schema(description = "조건 설명", example = "전원 서류 결과 입력 (보류 포함)")
            String label,

            @Schema(description = "충족 여부", example = "false")
            boolean satisfied,

            @Schema(description = "진행 현황", example = "12명 중 10명 입력")
            String detail
    ) {
    }
}

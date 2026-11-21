package com.example.cau_likelion_spring.recruitment.dto;

import com.example.cau_likelion_spring.recruitment.domain.ResultPublishStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/** 백오피스 전형별(서류·최종) 결과 공개 예약 현황 (상태·예약 일시·공개 조건 충족 여부) */
@Schema(description = "결과 공개 예약 현황")
public record ResultPublishResponse(

        @Schema(description = "상태 (INPUTTING: 입력 중, RESERVED: 공개 예약, PUBLISHED: 공개)", example = "INPUTTING")
        ResultPublishStatus status,

        @Schema(description = "공개 예약 일시 (입력 중이면 null)", example = "2027-03-10T18:00:00")
        LocalDateTime publishAt,

        @Schema(description = "결과 확인 종료일 (최종 결과만, 서류 결과이거나 예약 전이면 null)", example = "2027-05-31T23:59:00")
        LocalDateTime resultCheckDeadLine,

        @Schema(description = "[예약] 버튼 활성 여부 (입력 중이고 조건을 모두 만족하면 true)", example = "false")
        boolean reservable,

        @Schema(description = "[예약 취소] 버튼 활성 여부 (공개 예약 상태면 true. 단, 서류 결과는 최종 결과가 예약되어 있으면 false)", example = "false")
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

            @Schema(description = "충족 여부 (achieved == total)", example = "false")
            boolean satisfied,

            @Schema(description = "달성 수 (예: 서류 결과가 입력된 지원자 수)", example = "10")
            long achieved,

            @Schema(description = "전체 수 (예: 전체 지원자 수)", example = "12")
            long total,

            @Schema(description = "진행 현황", example = "12명 중 10명 입력")
            String detail
    ) {
    }
}

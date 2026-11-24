package com.example.cau_likelion_spring.recruitment.dto;

import com.example.cau_likelion_spring.recruitment.domain.Result;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 백오피스 확정 면접 일정 입력 응답.
 * 저장은 항상 되고, 확인이 필요한 상황이면 warnings에 담아 프론트가 경고를 띄운다.
 */
@Schema(description = "지원서별 확정 면접 일정 (경고 포함)")
public record InterviewScheduleResponse(

        @Schema(description = "지원서 id", example = "1")
        Long applicationId,

        @Schema(description = "확정 면접 일시", example = "2027-03-14T15:30:00")
        LocalDateTime fixedInterviewDate,

        @Schema(description = "면접 장소 (미입력이면 null)", example = "310관 728호")
        String fixedInterviewSpot,

        @Schema(description = "경고 목록 (없으면 빈 배열). 저장은 이미 완료된 상태입니다.")
        List<Warning> warnings
) {
    public static InterviewScheduleResponse of(Result result, List<Warning> warnings) {
        return new InterviewScheduleResponse(result.getApplication().getId(),
                result.getFixedInterviewDate(), result.getFixedInterviewSpot(), warnings);
    }

    public enum WarningType {
        /** 지원자가 고른 면접 가능 시간(15분 단위 슬롯) 밖의 일시 */
        OUT_OF_AVAILABLE_TIME,
        /** 같은 일시에 다른 지원자가 이미 배정됨 */
        DUPLICATE_SCHEDULE
    }

    @Schema(description = "확정 면접 일정 경고")
    public record Warning(

            @Schema(description = "경고 유형 (OUT_OF_AVAILABLE_TIME: 지원자 가능 시간 밖, DUPLICATE_SCHEDULE: 같은 일시 중복 배정)",
                    example = "OUT_OF_AVAILABLE_TIME")
            WarningType type,

            @Schema(description = "화면에 띄울 경고 문구", example = "지원자가 선택한 면접 가능 시간이 아닙니다.")
            String message
    ) {
    }
}

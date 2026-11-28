package com.example.cau_likelion_spring.recruitment.dto;

import com.example.cau_likelion_spring.recruitment.domain.FAQ;
import com.example.cau_likelion_spring.recruitment.domain.Round;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "모집공고 응답 (일정 + FAQ)")
public record RoundNoticeResponse(

        @Schema(description = "모집 회차 id (수정 API 경로에 사용)")
        Long roundId,

        @Schema(description = "서류접수 시작일시")
        LocalDateTime startDate,

        @Schema(description = "서류접수 마감일시")
        LocalDateTime deadLine,

        @Schema(description = "서류 결과발표 일시")
        LocalDateTime documentResultDate,

        @Schema(description = "면접 시작일시")
        LocalDateTime interviewStartDate,

        @Schema(description = "면접 종료일시")
        LocalDateTime interviewDeadLine,

        @Schema(description = "OT 일시")
        LocalDateTime otDate,

        @Schema(description = "FAQ 목록 (노출 순서대로)")
        List<RoundFaqResponse> faqs
) {
    /** faqs는 노출 순서대로 정렬된 상태로 넘겨야 한다 */
    public static RoundNoticeResponse of(Round round, List<FAQ> faqs) {
        return new RoundNoticeResponse(
                round.getId(),
                round.getStartDate(),
                round.getDeadLine(),
                round.getDocumentResultDate(),
                round.getInterviewStartDate(),
                round.getInterviewDeadLine(),
                round.getOtDate(),
                faqs.stream().map(RoundFaqResponse::from).toList()
        );
    }
}

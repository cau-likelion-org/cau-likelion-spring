package com.example.cau_likelion_spring.mypage.dto;

import com.example.cau_likelion_spring.member.domain.Member;
import com.example.cau_likelion_spring.mypage.domain.MemberScoreCalculator;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "상벌점 내역")
public record MemberScoreResponse(

        @Schema(description = "회원 id")
        Long memberId,

        @Schema(description = "이름", example = "홍길동")
        String name,

        @Schema(description = "기수", example = "14")
        Integer generationNumber,

        @Schema(description = "파트 이름", example = "백엔드")
        String partName,

        @Schema(description = "지각 횟수")
        long lateCount,

        @Schema(description = "결석 횟수")
        long absentCount,

        @Schema(description = "무단결석 횟수")
        long unauthorizedAbsentCount,

        @Schema(description = "과제 지각제출 횟수")
        long lateSubmitCount,

        @Schema(description = "과제 미제출 횟수")
        long missedCount,

        @Schema(description = "3점 만점 총점")
        double total
) {

    public static MemberScoreResponse of(Member member, long lateCount, long absentCount,
                                          long unauthorizedAbsentCount, long lateSubmitCount, long missedCount) {
        double total = MemberScoreCalculator.calculateTotal(
                lateCount, absentCount, unauthorizedAbsentCount, lateSubmitCount, missedCount);

        return new MemberScoreResponse(
                member.getId(),
                member.getName(),
                member.getPart() != null ? member.getPart().getGeneration().getNumber() : null,
                member.getPart() != null ? member.getPart().getName() : null,
                lateCount, absentCount, unauthorizedAbsentCount, lateSubmitCount, missedCount, total);
    }
}

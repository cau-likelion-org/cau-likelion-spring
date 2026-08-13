package com.example.cau_likelion_spring.assignment.dto;

import com.example.cau_likelion_spring.assignment.domain.AssignmentSubmitDisplayStatus;
import com.example.cau_likelion_spring.member.domain.Member;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "운영진이 보는 파트원 1명의 과제 제출 이력 전체 (재제출로 여러 건이면 모두 노출, 최신순)")
public record AssignmentMemberSubmissionHistoryResponse(

        @Schema(description = "멤버 ID", example = "1")
        Long memberId,

        @Schema(description = "멤버 이름")
        String memberName,

        @Schema(description = "화면 표시용 상태 (최신 제출 기준)")
        AssignmentSubmitDisplayStatus displayStatus,

        @Schema(description = "제출 이력 전체 (최신순, 제출 이력이 없으면 빈 배열)")
        List<AssignmentSubmitResponse> submissions
) {

    public static AssignmentMemberSubmissionHistoryResponse of(Member member, AssignmentSubmitDisplayStatus displayStatus,
                                                                 List<AssignmentSubmitResponse> submissions) {
        return new AssignmentMemberSubmissionHistoryResponse(member.getId(), member.getName(), displayStatus, submissions);
    }
}

package com.example.cau_likelion_spring.assignment.dto;

import com.example.cau_likelion_spring.assignment.domain.AssignmentSubmitDisplayStatus;
import com.example.cau_likelion_spring.member.domain.Member;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "운영진이 보는 파트원 1명의 과제 제출 현황 (제출했다면 최종본만 노출)")
public record AssignmentMemberSubmissionResponse(

        @Schema(description = "멤버 ID", example = "1")
        Long memberId,

        @Schema(description = "멤버 이름")
        String memberName,

        @Schema(description = "화면 표시용 상태")
        AssignmentSubmitDisplayStatus displayStatus,

        @Schema(description = "최종 제출본 (제출 이력이 없으면 null)")
        AssignmentSubmitResponse latestSubmission
) {

    public static AssignmentMemberSubmissionResponse of(Member member, AssignmentSubmitDisplayStatus displayStatus,
                                                          AssignmentSubmitResponse latestSubmission) {
        return new AssignmentMemberSubmissionResponse(member.getId(), member.getName(), displayStatus, latestSubmission);
    }
}

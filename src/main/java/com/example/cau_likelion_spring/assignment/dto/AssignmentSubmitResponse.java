package com.example.cau_likelion_spring.assignment.dto;

import com.example.cau_likelion_spring.assignment.domain.AssignmentSubmit;
import com.example.cau_likelion_spring.assignment.domain.AssignmentSubmitDisplayStatus;
import com.example.cau_likelion_spring.assignment.domain.AssignmentSubmitStatus;
import com.example.cau_likelion_spring.assignment.domain.SubmissionFile;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "과제 제출 응답")
public record AssignmentSubmitResponse(

        @Schema(description = "제출 ID", example = "1")
        Long id,

        @Schema(description = "과제 ID", example = "1")
        Long assignmentId,

        @Schema(description = "제출물 설명")
        String content,

        @Schema(description = "제출 URL")
        String url,

        @Schema(description = "제출 파일 목록")
        List<FileResponse> files,

        @Schema(description = "제출 상태")
        AssignmentSubmitStatus status,

        @Schema(description = "제출 시각 (수정 시 가장 최신 시각으로 갱신됨)")
        LocalDateTime submittedAt,

        @Schema(description = "화면 표시용 상태 (제출전/미제출/승인대기/지각제출/승인완료/승인반려)")
        AssignmentSubmitDisplayStatus displayStatus,

        @Schema(description = "평가(승인/반려)한 운영진 이름 (아직 평가 전이면 null)")
        String reviewerName,

        @Schema(description = "평가 일시")
        LocalDateTime approvalDate,

        @Schema(description = "반려 사유")
        String rejectionReason
) {

    @Schema(description = "제출 파일 응답")
    public record FileResponse(
            Long id,
            String fileUrl,
            String originalFilename
    ) {
        static FileResponse from(SubmissionFile file) {
            return new FileResponse(file.getId(), file.getFileUrl(), file.getOriginalFilename());
        }
    }

    public static AssignmentSubmitResponse of(AssignmentSubmit submit, List<SubmissionFile> files,
                                               AssignmentSubmitDisplayStatus displayStatus) {
        return new AssignmentSubmitResponse(
                submit.getId(),
                submit.getAssignment().getId(),
                submit.getContent(),
                submit.getUrl(),
                files.stream().map(FileResponse::from).toList(),
                submit.getStatus(),
                submit.getUpdatedAt(),
                displayStatus,
                submit.getReviewMember() == null ? null : submit.getReviewMember().getName(),
                submit.getApprovalDate(),
                submit.getRejectionReason()
        );
    }
}

package com.example.cau_likelion_spring.assignment.dto;

import com.example.cau_likelion_spring.assignment.domain.AssignmentSubmit;
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

        @Schema(description = "제출 일시")
        LocalDateTime createdAt,

        @Schema(description = "마감기한 이후 제출 여부 (지각제출)")
        boolean late,

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

    public static AssignmentSubmitResponse of(AssignmentSubmit submit, List<SubmissionFile> files, boolean late) {
        return new AssignmentSubmitResponse(
                submit.getId(),
                submit.getAssignment().getId(),
                submit.getContent(),
                submit.getUrl(),
                files.stream().map(FileResponse::from).toList(),
                submit.getStatus(),
                submit.getCreatedAt(),
                late,
                submit.getApprovalDate(),
                submit.getRejectionReason()
        );
    }
}

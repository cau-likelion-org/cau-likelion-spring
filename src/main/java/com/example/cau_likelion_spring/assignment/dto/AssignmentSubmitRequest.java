package com.example.cau_likelion_spring.assignment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "과제 제출/재제출 요청 (Assignment.type이 URL이면 url을, FILE이면 files를 채워야 함)")
public record AssignmentSubmitRequest(

        @Schema(description = "제출물 설명 (선택, 공백 포함 최대 300자)")
        @Size(max = 300) String content,

        @Schema(description = "제출 URL (Assignment.type = URL인 경우 필수)")
        String url,

        @Schema(description = "제출 파일 목록 (Assignment.type = FILE인 경우 1개 이상 필수, 개수 제한 없음)")
        List<@Valid FileInfo> files
) {

    @Schema(description = "제출 파일 정보 (파일 자체는 별도 스토리지 API로 업로드 후 그 결과를 전달)")
    public record FileInfo(
            @Schema(description = "스토리지에 저장된 파일 URL")
            @NotBlank String fileUrl,

            @Schema(description = "원본 파일명")
            @NotBlank String originalFilename
    ) {
    }
}

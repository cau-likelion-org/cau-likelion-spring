package com.example.cau_likelion_spring.global.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record FileUploadResponse(

        @Schema(description = "업로드된 파일의 S3 URL", example = "https://버킷.s3.ap-northeast-2.amazonaws.com/project/uuid.jpg")
        String url
) {
}

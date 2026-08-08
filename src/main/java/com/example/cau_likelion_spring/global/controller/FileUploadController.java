package com.example.cau_likelion_spring.global.controller;

import com.example.cau_likelion_spring.global.dto.FileUploadResponse;
import com.example.cau_likelion_spring.global.util.S3Uploader;
import com.example.cau_likelion_spring.global.util.UploadDomain;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "File Upload", description = "이미지/파일 업로드(S3) API")
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileUploadController {

    private final S3Uploader s3Uploader;

    @Operation(summary = "이미지/파일 업로드", description = "파일을 S3에 업로드하고 URL을 반환합니다. "
            + "이 URL을 각 도메인(프로젝트/히스토리/세션/활동/과제)의 생성·수정 API에 그대로 넣어 사용합니다. "
            + "도메인별로 저장 폴더와 허용 파일 형식·용량 제한이 다릅니다 (PROJECT/HISTORY/SESSION/ACTIVITY는 이미지 전용, ASSIGNMENT는 문서 파일도 허용).")
    @PostMapping("/{domain}")
    public ResponseEntity<FileUploadResponse> upload(
            @Parameter(description = "업로드 대상 도메인", required = true) @PathVariable UploadDomain domain,
            @RequestParam MultipartFile file) {
        String url = s3Uploader.upload(file, domain);
        return ResponseEntity.ok(new FileUploadResponse(url));
    }
}

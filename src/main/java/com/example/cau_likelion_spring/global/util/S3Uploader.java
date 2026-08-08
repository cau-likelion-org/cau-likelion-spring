package com.example.cau_likelion_spring.global.util;

import com.example.cau_likelion_spring.global.exception.CustomException;
import com.example.cau_likelion_spring.global.exception.ErrorCode;
import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Resource;
import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.util.Locale;
import java.util.UUID;

/** S3 업로드/삭제 + 검증(확장자/용량)을 담당한다. 어떤 도메인 폴더에, 어떤 파일까지 허용할지는 {@link UploadDomain}이 정의한다. */
@Slf4j
@Component
@RequiredArgsConstructor
public class S3Uploader {

    private final S3Template s3Template;

    @Value("${app.s3.bucket}")
    private String bucket;

    public String upload(MultipartFile file, UploadDomain domain) {
        validate(file, domain);

        String key = domain.getFolder() + "/" + UUID.randomUUID() + "." + extractExtension(file.getOriginalFilename());
        ObjectMetadata metadata = ObjectMetadata.builder()
                .contentType(file.getContentType())
                .build();

        try {
            S3Resource resource = s3Template.upload(bucket, key, file.getInputStream(), metadata);
            return resource.getURL().toString();
        } catch (IOException e) {
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    /**
     * 이 클래스가 발급한 URL을 대상으로 S3 파일을 삭제한다.
     * 실패해도 예외를 던지지 않고 로그만 남긴다 - S3 일시 장애 때문에 정상적인 수정/삭제 요청 자체가 실패하면 안 되므로 best-effort로 처리.
     */
    public void deleteByUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return;
        }

        try {
            String key = extractKey(url);
            s3Template.deleteObject(bucket, key);
        } catch (Exception e) {
            log.warn("S3 파일 삭제 실패 (무시하고 진행): url={}", url, e);
        }
    }

    private String extractKey(String url) {
        String path = URI.create(url).getPath();
        return path.startsWith("/") ? path.substring(1) : path;
    }

    private void validate(MultipartFile file, UploadDomain domain) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT, "업로드할 파일이 없습니다.");
        }
        if (file.getSize() > domain.getMaxSizeBytes()) {
            throw new CustomException(ErrorCode.FILE_SIZE_EXCEEDED);
        }
        if (!domain.getAllowedExtensions().contains(extractExtension(file.getOriginalFilename()))) {
            throw new CustomException(ErrorCode.INVALID_FILE_TYPE,
                    "허용되지 않는 파일 형식입니다. 허용 확장자: " + domain.getAllowedExtensions());
        }
    }

    private String extractExtension(String originalFilename) {
        if (!StringUtils.hasText(originalFilename) || !originalFilename.contains(".")) {
            throw new CustomException(ErrorCode.INVALID_FILE_TYPE, "파일 확장자를 확인할 수 없습니다.");
        }
        return originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }
}

package com.example.cau_likelion_spring.global.util;

import com.example.cau_likelion_spring.global.exception.CustomException;
import com.example.cau_likelion_spring.global.exception.ErrorCode;
import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Resource;
import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/** S3 업로드/삭제 + 검증(확장자/용량)을 담당한다. 어떤 도메인 폴더에, 어떤 파일까지 허용할지는 {@link UploadDomain}이 정의한다. */
@Slf4j
@Component
@RequiredArgsConstructor
public class S3Uploader {

    // 리사이징 결과의 가로/세로 최대 픽셀 - 웹에서 실제 표시되는 크기보다 넉넉하게 잡아, 고해상도 화면에서도 화질 저하가 안 보이게 함
    private static final int MAX_DIMENSION = 1920;
    private static final double OUTPUT_QUALITY = 0.85;

    // Thumbnailator가 기본 지원하는 포맷만 리사이징 대상으로 함.
    // gif는 애니메이션이 깨질 수 있고, webp는 JDK가 기본 지원하지 않아 리사이징 시도 시 에러가 나므로 원본 그대로 업로드한다.
    private static final Set<String> RESIZABLE_EXTENSIONS = Set.of("jpg", "jpeg", "png");

    private final S3Template s3Template;

    @Value("${app.s3.bucket}")
    private String bucket;

    public String upload(MultipartFile file, UploadDomain domain) {
        validate(file, domain);

        String extension = extractExtension(file.getOriginalFilename());
        byte[] content = resizeIfNeeded(file, domain, extension);

        String key = domain.getFolder() + "/" + UUID.randomUUID() + "." + extension;
        ObjectMetadata metadata = ObjectMetadata.builder()
                .contentType(file.getContentType())
                .build();

        try {
            S3Resource resource = s3Template.upload(bucket, key, new ByteArrayInputStream(content), metadata);
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

    /**
     * 리사이징 대상 도메인(UploadDomain.resizable=true) + 지원 포맷(jpg/jpeg/png)인 경우에만 리사이징한다.
     * 원본이 이미 MAX_DIMENSION보다 작으면 리사이징을 건너뛰고 원본을 그대로 사용한다 - 절대 확대(업스케일)하지 않는다.
     * <p>
     * 메모리 최적화: ImageIO.read()로 픽셀을 100% 디코딩하는 대신, 먼저 헤더에서 가로/세로만 읽어
     * 리사이징이 필요한지 판단하고, 필요한 경우에만 서브샘플링(setSourceSubsampling)으로 이미 축소된
     * 해상도로 디코딩한다. 원본을 통째로 펼친 뒤 축소하면(4000x3000 원본이 픽셀당 4바이트 기준 약 48MB까지
     * 커질 수 있음) 사진 여러 장이 동시에 처리될 때 OOM 위험이 커지므로, 애초에 필요한 크기로만 디코딩해서
     * 순간 메모리 사용량을 크게 줄인다.
     * 리사이징이 실패하면(손상된 파일 등) 업로드 자체를 막지 않고 원본을 그대로 사용한다 - best-effort.
     */
    private byte[] resizeIfNeeded(MultipartFile file, UploadDomain domain, String extension) {
        if (!domain.isResizable() || !RESIZABLE_EXTENSIONS.contains(extension)) {
            return readAllBytes(file);
        }

        byte[] originalBytes = readAllBytes(file);

        try (ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(originalBytes))) {
            ImageReader reader = findImageReader(iis, file.getOriginalFilename());
            if (reader == null) {
                // ImageIO가 못 읽는 파일(손상됐거나 예상 못한 포맷) - 리사이징 없이 원본 그대로 업로드
                return originalBytes;
            }

            try {
                reader.setInput(iis, true, true);
                int originalWidth = reader.getWidth(0);
                int originalHeight = reader.getHeight(0);

                // 원본이 이미 목표 크기 이하면 확대하지 않고 원본을 그대로 사용 (디코딩 자체를 안 해서 더 빠름)
                if (originalWidth <= MAX_DIMENSION && originalHeight <= MAX_DIMENSION) {
                    return originalBytes;
                }

                // 축소할 배수만큼만 디코딩하도록 서브샘플링 설정 - 원본을 100% 펼치지 않고 처음부터 줄여서 읽음
                int subsampling = Math.max(1, Math.min(originalWidth, originalHeight) / MAX_DIMENSION);
                ImageReadParam param = reader.getDefaultReadParam();
                param.setSourceSubsampling(subsampling, subsampling, 0, 0);

                BufferedImage image = reader.read(0, param);

                ByteArrayOutputStream resized = new ByteArrayOutputStream();
                Thumbnails.of(image)
                        .size(MAX_DIMENSION, MAX_DIMENSION)
                        .outputQuality(OUTPUT_QUALITY)
                        .outputFormat(extension.equals("jpg") ? "jpeg" : extension)
                        .toOutputStream(resized);
                return resized.toByteArray();
            } finally {
                reader.dispose();
            }
        } catch (IOException e) {
            log.warn("이미지 리사이징 실패, 원본으로 업로드합니다: filename={}", file.getOriginalFilename(), e);
            return originalBytes;
        }
    }

    private ImageReader findImageReader(ImageInputStream iis, String filename) {
        Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
        if (!readers.hasNext()) {
            log.warn("이미지를 읽을 수 없어 리사이징을 건너뜁니다: filename={}", filename);
            return null;
        }
        return readers.next();
    }

    private byte[] readAllBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
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

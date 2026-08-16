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
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/** S3 업로드/삭제 + 검증(확장자/용량)을 담당한다. 어떤 도메인 폴더에, 어떤 파일까지 허용할지는 {@link UploadDomain}이 정의한다. */
@Slf4j
@Component
@RequiredArgsConstructor
public class S3Uploader {

    // 리사이징 결과의 가로/세로 최대 픽셀 - 웹에서 실제 표시되는 크기보다 넉넉하게 잡아, 고해상도 화면에서도 화질 저하가 안 보이게 함
    private static final int MAX_DIMENSION = 1920;
    private static final double OUTPUT_QUALITY = 0.85;

    // Thumbnailator(=ImageIO)가 지원하는 포맷만 리사이징 대상으로 함.
    // jpg/jpeg/png는 JDK 표준 지원, webp는 별도 플러그인(webp-imageio)을 추가해서 지원한다.
    // gif는 리사이징하면 애니메이션이 깨지므로 대상에서 제외하고 원본 그대로 업로드한다.
    private static final Set<String> RESIZABLE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");

    // 리사이징(디코딩~인코딩)은 순간적으로 메모리를 많이 쓰므로, t3.micro(메모리 1GB) 기준으로
    // 동시에 최대 2건까지만 처리하도록 제한한다. 자리가 없으면 최대 5초 대기하고, 그래도 안 되면 실패 처리한다.
    // (무한 대기시키면 어딘가 막혔을 때 요청이 영원히 안 끝날 수 있어 타임아웃을 둔다)
    private static final int MAX_CONCURRENT_RESIZE = 2;
    private static final long RESIZE_WAIT_TIMEOUT_SECONDS = 5;
    private final Semaphore resizeSemaphore = new Semaphore(MAX_CONCURRENT_RESIZE);

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
     * 메모리 최적화 1: ImageIO.read()로 픽셀을 100% 디코딩하는 대신, 먼저 헤더에서 가로/세로만 읽어
     * 리사이징이 필요한지 판단하고, 필요한 경우에만 서브샘플링(setSourceSubsampling)으로 이미 축소된
     * 해상도로 디코딩한다.
     * 메모리 최적화 2: 그래도 여러 장이 정확히 동시에 몰리면 순간 메모리가 쌓일 수 있으므로,
     * 세마포어로 동시 리사이징 개수 자체를 MAX_CONCURRENT_RESIZE로 제한한다.
     * 리사이징이 실패하면(손상된 파일 등) 업로드 자체를 막지 않고 원본을 그대로 사용한다 - best-effort.
     * 단, 동시 처리 자리를 못 구한 경우(서버가 바쁨)는 원본 업로드로 눈감아주지 않고 명시적으로 실패시킨다 -
     * 안전장치를 우회해서 결국 메모리를 또 많이 쓰게 되면 세마포어를 두는 의미가 없기 때문이다.
     */
    private byte[] resizeIfNeeded(MultipartFile file, UploadDomain domain, String extension) {
        if (!domain.isResizable() || !RESIZABLE_EXTENSIONS.contains(extension)) {
            return readAllBytes(file);
        }

        byte[] originalBytes = readAllBytes(file);

        // 먼저 헤더만 가볍게 읽어서, 애초에 리사이징이 필요 없는 작은 이미지면 세마포어 자리도 안 쓰고 바로 반환
        Dimension dimension = readDimension(originalBytes, file.getOriginalFilename());
        if (dimension == null) {
            return originalBytes;
        }
        if (dimension.width() <= MAX_DIMENSION && dimension.height() <= MAX_DIMENSION) {
            return originalBytes;
        }

        boolean acquired;
        try {
            acquired = resizeSemaphore.tryAcquire(RESIZE_WAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CustomException(ErrorCode.IMAGE_PROCESSING_BUSY);
        }
        if (!acquired) {
            throw new CustomException(ErrorCode.IMAGE_PROCESSING_BUSY);
        }

        try {
            return resize(originalBytes, dimension, extension, file.getOriginalFilename());
        } finally {
            resizeSemaphore.release();
        }
    }

    /** 픽셀을 디코딩하지 않고 이미지 헤더만 읽어 가로/세로를 확인한다. 읽을 수 없는 파일이면 null을 반환한다. */
    private Dimension readDimension(byte[] imageBytes, String filename) {
        try (ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(imageBytes))) {
            ImageReader reader = findImageReader(iis, filename);
            if (reader == null) {
                return null;
            }
            try {
                reader.setInput(iis, true, true);
                return new Dimension(reader.getWidth(0), reader.getHeight(0));
            } finally {
                reader.dispose();
            }
        } catch (IOException e) {
            log.warn("이미지 헤더를 읽을 수 없어 리사이징을 건너뜁니다: filename={}", filename, e);
            return null;
        }
    }

    /** 서브샘플링으로 축소된 해상도로 디코딩한 뒤, Thumbnailator로 정밀하게 MAX_DIMENSION에 맞춘다. */
    private byte[] resize(byte[] originalBytes, Dimension original, String extension, String filename) {
        try (ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(originalBytes))) {
            ImageReader reader = findImageReader(iis, filename);
            if (reader == null) {
                return originalBytes;
            }

            try {
                reader.setInput(iis, true, true);

                // 축소할 배수만큼만 디코딩하도록 서브샘플링 설정 - 원본을 100% 펼치지 않고 처음부터 줄여서 읽음
                int subsampling = Math.max(1, Math.min(original.width(), original.height()) / MAX_DIMENSION);
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
            log.warn("이미지 리사이징 실패, 원본으로 업로드합니다: filename={}", filename, e);
            return originalBytes;
        }
    }

    private record Dimension(int width, int height) {
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

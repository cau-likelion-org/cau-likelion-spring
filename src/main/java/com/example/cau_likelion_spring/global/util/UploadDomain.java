package com.example.cau_likelion_spring.global.util;

import lombok.Getter;

import java.util.Set;

/**
 * 이미지/파일 업로드가 저장될 S3 폴더와, 도메인별로 허용하는 파일 확장자·최대 용량을 정의한다.
 * PROJECT/HISTORY/SESSION/ACTIVITY/ROADMAP은 이미지 전용이고, ASSIGNMENT(과제 첨부파일)만 문서 파일도 허용한다.
 * resizable=true인 도메인은 S3 업로드 전에 리사이징을 거친다 (ASSIGNMENT는 이미지/문서가 섞여있어 리사이징 대상에서 제외).
 * maxSizeBytes는 "업로드를 허용하는 원본 크기" 기준이며, 리사이징 대상 도메인은 스마트폰 원본 사진(8~15MB대)도
 * 받을 수 있도록 넉넉하게 잡혀있다 - 실제 저장되는 최종 용량은 리사이징 후 훨씬 작아진다.
 * forceDownload=true인 도메인은 S3 객체에 Content-Disposition: attachment를 실어, 브라우저가 이미지도
 * 새 탭에 열지 않고 항상 다운로드하도록 한다 (ASSIGNMENT: 운영진이 아기사자 제출 파일을 내려받아 확인해야 함).
 * 다른 도메인은 갤러리/썸네일 등 화면에 바로 표시돼야 하므로 inline으로 유지한다.
 */
@Getter
public enum UploadDomain {

    PROJECT("project", imageExtensions(), 20 * 1024 * 1024L, true, false),
    HISTORY("history", imageExtensions(), 20 * 1024 * 1024L, true, false),
    SESSION("session", imageExtensions(), 20 * 1024 * 1024L, true, false),
    ACTIVITY("activity", imageExtensions(), 20 * 1024 * 1024L, true, false),
    ROADMAP("roadmap", imageExtensions(), 20 * 1024 * 1024L, true, false),
    ASSIGNMENT("assignment", Set.of("jpg", "jpeg", "png", "webp", "gif", "pdf", "zip", "doc", "docx", "ppt", "pptx"), 10 * 1024 * 1024L, false, true);

    private final String folder;
    private final Set<String> allowedExtensions;
    private final long maxSizeBytes;
    private final boolean resizable;
    private final boolean forceDownload;

    UploadDomain(String folder, Set<String> allowedExtensions, long maxSizeBytes, boolean resizable, boolean forceDownload) {
        this.folder = folder;
        this.allowedExtensions = allowedExtensions;
        this.maxSizeBytes = maxSizeBytes;
        this.resizable = resizable;
        this.forceDownload = forceDownload;
    }

    private static Set<String> imageExtensions() {
        return Set.of("jpg", "jpeg", "png", "webp");
    }
}

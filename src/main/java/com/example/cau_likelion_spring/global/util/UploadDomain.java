package com.example.cau_likelion_spring.global.util;

import lombok.Getter;

import java.util.Set;

/**
 * 이미지/파일 업로드가 저장될 S3 폴더와, 도메인별로 허용하는 파일 확장자·최대 용량을 정의한다.
 * PROJECT/HISTORY/SESSION/ACTIVITY/ROADMAP은 이미지 전용이고, ASSIGNMENT(과제 첨부파일)만 문서 파일도 허용한다.
 */
@Getter
public enum UploadDomain {

    PROJECT("project", imageExtensions(), 5 * 1024 * 1024L),
    HISTORY("history", imageExtensions(), 5 * 1024 * 1024L),
    SESSION("session", imageExtensions(), 5 * 1024 * 1024L),
    ACTIVITY("activity", imageExtensions(), 5 * 1024 * 1024L),
    ROADMAP("roadmap", imageExtensions(), 5 * 1024 * 1024L),
    ASSIGNMENT("assignment", Set.of("jpg", "jpeg", "png", "webp", "gif", "pdf", "zip", "doc", "docx", "ppt", "pptx"), 10 * 1024 * 1024L);

    private final String folder;
    private final Set<String> allowedExtensions;
    private final long maxSizeBytes;

    UploadDomain(String folder, Set<String> allowedExtensions, long maxSizeBytes) {
        this.folder = folder;
        this.allowedExtensions = allowedExtensions;
        this.maxSizeBytes = maxSizeBytes;
    }

    private static Set<String> imageExtensions() {
        return Set.of("jpg", "jpeg", "png", "webp", "gif");
    }
}

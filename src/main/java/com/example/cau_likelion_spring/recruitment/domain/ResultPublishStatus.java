package com.example.cau_likelion_spring.recruitment.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

/**
 * 전형별(서류·최종) 결과 공개 상태.
 * 별도 상태 컬럼 없이 예약 시각과 현재 시각으로 판정한다 → 예약 시각이 되면 스케줄러 없이 자동으로 공개된다.
 */
@Getter
@RequiredArgsConstructor
public enum ResultPublishStatus {
    INPUTTING("입력 중"),
    RESERVED("공개 예약"),
    PUBLISHED("공개");

    private final String label;

    public static ResultPublishStatus of(LocalDateTime publishAt, LocalDateTime now) {
        if (publishAt == null) {
            return INPUTTING;
        }
        return now.isBefore(publishAt) ? RESERVED : PUBLISHED;
    }
}

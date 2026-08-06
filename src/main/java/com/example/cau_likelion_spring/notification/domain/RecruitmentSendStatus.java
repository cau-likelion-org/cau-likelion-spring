package com.example.cau_likelion_spring.notification.domain;

/**
 * 모집 공고(RecruitmentText) 자체의 발송 진행 상태. 수신자별 발송 성공/실패를 나타내는 EmailSentStatus와는 별개.
 */
public enum RecruitmentSendStatus {
    SCHEDULED,
    SENT,
    CANCELLED
}

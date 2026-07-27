package com.example.cau_likelion_spring.notification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 신청자에게 모집 공고 이메일을 발송(예정/완료)한 이력
 * 생성 시점에는 PENDING 상태이며 sentAt은 null, 실제 발송 시도 후 markSent()로 상태와 발송 시각이 채워짐
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailSentLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscriber_id", nullable = false)
    private RecruitmentSubscriber subscriber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "text_id", nullable = false)
    private RecruitmentText recruitmentText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20)")
    private EmailSentStatus status;

    private LocalDateTime sentAt;

    @Builder
    public EmailSentLog(RecruitmentSubscriber subscriber, RecruitmentText recruitmentText) {
        this.subscriber = subscriber;
        this.recruitmentText = recruitmentText;
        this.status = EmailSentStatus.PENDING;
    }

    public void markSent(EmailSentStatus status) {
        this.status = status;
        this.sentAt = LocalDateTime.now();
    }
}

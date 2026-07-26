package com.example.cau_likelion_spring.notification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
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
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 신청자에게 모집 공고 이메일을 발송한 이력
 */
@Entity
@Getter
@EntityListeners(AuditingEntityListener.class)
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
    @Column(nullable = false)
    private EmailSentStatus status;

    @CreatedDate
    private LocalDateTime sentAt;

    @Builder
    public EmailSentLog(RecruitmentSubscriber subscriber, RecruitmentText recruitmentText, EmailSentStatus status) {
        this.subscriber = subscriber;
        this.recruitmentText = recruitmentText;
        this.status = status;
    }
}

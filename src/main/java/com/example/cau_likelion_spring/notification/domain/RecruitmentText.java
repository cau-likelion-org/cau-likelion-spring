package com.example.cau_likelion_spring.notification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 모집 알림 이메일 본문
 */
@Entity
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecruitmentText {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Lob
    private String content;

    /** 예정 전송일 */
    private LocalDateTime scheduledSendAt;

    @CreatedDate
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20)")
    private RecruitmentSendStatus status;

    @Builder
    public RecruitmentText(String title, String content, LocalDateTime scheduledSendAt) {
        this.title = title;
        this.content = content;
        this.scheduledSendAt = scheduledSendAt;
        this.status = RecruitmentSendStatus.SCHEDULED;
    }

    public void update(String title, String content, LocalDateTime scheduledSendAt) {
        this.title = title;
        this.content = content;
        this.scheduledSendAt = scheduledSendAt;
    }

    public void markSent() {
        this.status = RecruitmentSendStatus.SENT;
    }

    public void cancel() {
        this.status = RecruitmentSendStatus.CANCELLED;
    }
}

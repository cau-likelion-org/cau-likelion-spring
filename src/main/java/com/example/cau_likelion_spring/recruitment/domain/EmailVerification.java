package com.example.cau_likelion_spring.recruitment.domain;

import com.example.cau_likelion_spring.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 지원자 이메일 인증번호 (7.3.1, 7.3.4).
 * 이메일·용도마다 한 행만 두고, 재발송하면 같은 행을 덮어쓴다.
 */
@Entity
@Table(name = "recruitment_email_verification",
        uniqueConstraints = @UniqueConstraint(columnNames = {"email", "purpose"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailVerification extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EmailVerificationPurpose purpose;

    @Column(nullable = false, length = 6)
    private String code;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    /** 인증번호 확인 시도 횟수. 비교 전에 먼저 올리므로 최대 횟수에 도달하면 이 번호는 더 쓸 수 없다 */
    @Column(nullable = false)
    private int failCount;

    /** 마지막 발송 시각 (재발송 간격 제한용) */
    @Column(nullable = false)
    private LocalDateTime sentAt;

    @Builder
    public EmailVerification(String email, EmailVerificationPurpose purpose, String code,
                             LocalDateTime expiresAt, LocalDateTime sentAt) {
        this.email = email;
        this.purpose = purpose;
        this.code = code;
        this.expiresAt = expiresAt;
        this.sentAt = sentAt;
    }

    /** 재발송: 새 인증번호로 덮어쓰고 시도 횟수를 초기화한다 */
    public void renew(String code, LocalDateTime expiresAt, LocalDateTime sentAt) {
        this.code = code;
        this.expiresAt = expiresAt;
        this.sentAt = sentAt;
        this.failCount = 0;
    }
}

package com.example.cau_likelion_spring.recruitment.scheduler;

import com.example.cau_likelion_spring.recruitment.service.EmailVerificationStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/** 만료된 인증번호 행을 하루 한 번 삭제한다 (확인하지 않고 버려진 인증번호가 쌓이지 않게) */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailVerificationCleanupScheduler {

    private final EmailVerificationStore emailVerificationStore;

    @Scheduled(cron = "${recruitment.email-verification.cleanup-cron}")
    public void deleteExpired() {
        int deleted = emailVerificationStore.deleteExpired(LocalDateTime.now());
        log.info("만료된 이메일 인증번호 {}건 삭제", deleted);
    }
}

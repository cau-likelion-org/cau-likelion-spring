package com.example.cau_likelion_spring.recruitment.service;

import com.example.cau_likelion_spring.recruitment.domain.EmailVerification;
import com.example.cau_likelion_spring.recruitment.domain.EmailVerificationPurpose;
import com.example.cau_likelion_spring.recruitment.repository.EmailVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 인증번호 저장소. 지금은 DB 테이블에 저장한다 (EC2 메모리 여유가 없어 Redis 미사용).
 * 저장 방식을 바꿀 때 이 클래스만 교체하면 되도록 저장·조회·삭제를 여기에 모은다.
 */
@Component
@RequiredArgsConstructor
public class EmailVerificationStore {

    private final EmailVerificationRepository emailVerificationRepository;

    public Optional<EmailVerification> find(String email, EmailVerificationPurpose purpose) {
        return emailVerificationRepository.findByEmailAndPurpose(email, purpose);
    }

    /** 같은 이메일·용도로 동시에 처음 저장하면 unique 위반이 바로 드러나도록 즉시 flush한다 */
    public void save(EmailVerification verification) {
        emailVerificationRepository.saveAndFlush(verification);
    }

    /** 확인 시도 1회를 기록한다. 이미 최대 횟수를 채웠으면 false */
    public boolean countAttempt(Long id, int maxFailCount) {
        return emailVerificationRepository.increaseFailCount(id, maxFailCount) > 0;
    }

    public void delete(Long id) {
        emailVerificationRepository.deleteById(id);
    }

    public int deleteExpired(LocalDateTime now) {
        return emailVerificationRepository.deleteAllExpired(now);
    }
}

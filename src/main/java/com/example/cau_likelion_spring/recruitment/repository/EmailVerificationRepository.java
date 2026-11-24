package com.example.cau_likelion_spring.recruitment.repository;

import com.example.cau_likelion_spring.recruitment.domain.EmailVerification;
import com.example.cau_likelion_spring.recruitment.domain.EmailVerificationPurpose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {

    Optional<EmailVerification> findByEmailAndPurpose(String email, EmailVerificationPurpose purpose);

    /**
     * 시도 횟수가 최대 횟수 미만일 때만 1 올린다. 올렸으면 1, 이미 최대 횟수면 0을 반환한다.
     * 조회 후 저장하지 않고 update 한 번으로 처리해서, 동시에 여러 번 시도해도 최대 횟수를 넘지 않는다.
     */
    @Modifying
    @Transactional
    @Query("update EmailVerification v set v.failCount = v.failCount + 1 "
            + "where v.id = :id and v.failCount < :maxFailCount")
    int increaseFailCount(@Param("id") Long id, @Param("maxFailCount") int maxFailCount);

    @Modifying
    @Transactional
    @Query("delete from EmailVerification v where v.expiresAt < :now")
    int deleteAllExpired(@Param("now") LocalDateTime now);
}

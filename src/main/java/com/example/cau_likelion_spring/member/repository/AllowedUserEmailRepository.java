package com.example.cau_likelion_spring.member.repository;

import com.example.cau_likelion_spring.member.domain.AllowedUserEmail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AllowedUserEmailRepository extends JpaRepository<AllowedUserEmail, Long> {

    Optional<AllowedUserEmail> findByAllowedEmailAndGeneration_IdAndIsJoinedFalse(String allowedEmail, Long generationId);

    /**
     * 구글 로그인 콜백 시점엔 아직 기수를 모르므로, "가입 폼을 보여줄지"만 판단하는 1차 체크용.
     * 실제 기수 일치 여부는 join() 시점에 findByAllowedEmailAndGeneration_IdAndIsJoinedFalse로 다시 검증한다.
     */
    boolean existsByAllowedEmailAndIsJoinedFalse(String allowedEmail);
}

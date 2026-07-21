package com.example.cau_likelion_spring.member.repository;

import com.example.cau_likelion_spring.member.domain.AllowedUserEmail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AllowedUserEmailRepository extends JpaRepository<AllowedUserEmail, Long> {

    Optional<AllowedUserEmail> findByAllowedEmailAndGeneration_IdAndIsJoinedFalse(String allowedEmail, Long generationId);
}

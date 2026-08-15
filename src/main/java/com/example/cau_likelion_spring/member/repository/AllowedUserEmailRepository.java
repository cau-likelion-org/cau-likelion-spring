package com.example.cau_likelion_spring.member.repository;

import com.example.cau_likelion_spring.member.domain.AllowedUserEmail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AllowedUserEmailRepository extends JpaRepository<AllowedUserEmail, Long> {

    Optional<AllowedUserEmail> findByAllowedEmailAndGeneration_Id(String allowedEmail, Long generationId);

    boolean existsByAllowedEmail(String allowedEmail);

    List<AllowedUserEmail> findAllByGeneration_Id(Long generationId);

    void deleteAllByAllowedEmail(String allowedEmail);
}

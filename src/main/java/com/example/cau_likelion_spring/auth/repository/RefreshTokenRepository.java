package com.example.cau_likelion_spring.auth.repository;

import com.example.cau_likelion_spring.auth.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByMember_Id(Long memberId);

    void deleteByToken(String token);
}

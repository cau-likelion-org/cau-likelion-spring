package com.example.cau_likelion_spring.member.repository;

import com.example.cau_likelion_spring.member.domain.FcmToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {

    Optional<FcmToken> findByToken(String token);

    List<FcmToken> findAllByMember_Id(Long memberId);

    void deleteByMember_IdAndToken(Long memberId, String token);
}

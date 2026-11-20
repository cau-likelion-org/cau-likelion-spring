package com.example.cau_likelion_spring.organization.repository;

import com.example.cau_likelion_spring.organization.domain.Generation;
import com.example.cau_likelion_spring.organization.domain.GenerationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GenerationRepository extends JpaRepository<Generation, Long> {

    Optional<Generation> findByStatus(GenerationStatus status);

    Optional<Generation> findTopByOrderByNumberDesc();

    /** 기수 번호로 조회 (번호에 DB unique 제약이 없어 같은 번호가 여러 개면 가장 최근에 만든 기수) */
    Optional<Generation> findFirstByNumberOrderByIdDesc(Integer number);
}

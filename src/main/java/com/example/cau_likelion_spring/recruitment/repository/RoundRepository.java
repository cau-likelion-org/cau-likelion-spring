package com.example.cau_likelion_spring.recruitment.repository;

import com.example.cau_likelion_spring.recruitment.domain.Round;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoundRepository extends JpaRepository<Round, Long> {

    /** 현재 회차 = 가장 최근에 만든 회차 */
    Optional<Round> findTopByOrderByIdDesc();

    /** 전체 회차 (최근에 만든 회차 먼저) */
    List<Round> findAllByOrderByIdDesc();
}

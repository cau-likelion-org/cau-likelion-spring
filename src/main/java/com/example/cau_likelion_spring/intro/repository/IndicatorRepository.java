package com.example.cau_likelion_spring.intro.repository;

import com.example.cau_likelion_spring.intro.domain.Indicator;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IndicatorRepository extends JpaRepository<Indicator, Long> {

    // 단일 엔티티 정책이므로 항상 첫 번째(=유일한) row를 가져온다
    Optional<Indicator> findFirstByOrderByIdAsc();
}

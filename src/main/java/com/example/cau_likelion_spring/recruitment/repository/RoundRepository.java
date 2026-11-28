package com.example.cau_likelion_spring.recruitment.repository;

import com.example.cau_likelion_spring.recruitment.domain.Round;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoundRepository extends JpaRepository<Round, Long> {

    /** 현재 회차 = 가장 최근에 만든 회차 */
    Optional<Round> findTopByOrderByIdDesc();

    /** 전체 회차 (최근에 만든 회차 먼저) */
    List<Round> findAllByOrderByIdDesc();

    /**
     * 회차 행에 쓰기 락을 걸고 조회한다 (SELECT ... FOR UPDATE).
     * 통계 스냅샷을 회차당 한 번만 만들기 위해, 동시에 들어온 요청을 줄 세우는 데 쓴다.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Round r WHERE r.id = :id")
    Optional<Round> findByIdForUpdate(@Param("id") Long id);
}

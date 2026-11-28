package com.example.cau_likelion_spring.recruitment.repository;

import com.example.cau_likelion_spring.recruitment.domain.Round;
import com.example.cau_likelion_spring.recruitment.domain.StatsSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StatsSnapshotRepository extends JpaRepository<StatsSnapshot, Long> {

    List<StatsSnapshot> findAllByRound(Round round);

    boolean existsByRound(Round round);
}

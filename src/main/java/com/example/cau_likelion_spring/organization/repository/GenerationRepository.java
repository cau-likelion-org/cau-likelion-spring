package com.example.cau_likelion_spring.organization.repository;

import com.example.cau_likelion_spring.organization.domain.Generation;
import com.example.cau_likelion_spring.organization.domain.GenerationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GenerationRepository extends JpaRepository<Generation, Long> {

    Optional<Generation> findByStatus(GenerationStatus status);

    Optional<Generation> findTopByOrderByNumberDesc();
}

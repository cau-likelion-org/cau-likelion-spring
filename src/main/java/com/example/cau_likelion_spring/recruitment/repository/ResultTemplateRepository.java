package com.example.cau_likelion_spring.recruitment.repository;

import com.example.cau_likelion_spring.recruitment.domain.ResultTemplate;
import com.example.cau_likelion_spring.recruitment.domain.ResultTemplateType;
import com.example.cau_likelion_spring.recruitment.domain.Round;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResultTemplateRepository extends JpaRepository<ResultTemplate, Long> {

    Optional<ResultTemplate> findByRoundAndType(Round round, ResultTemplateType type);
}

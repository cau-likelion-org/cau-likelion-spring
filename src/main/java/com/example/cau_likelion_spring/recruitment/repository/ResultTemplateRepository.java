package com.example.cau_likelion_spring.recruitment.repository;

import com.example.cau_likelion_spring.recruitment.domain.ResultTemplate;
import com.example.cau_likelion_spring.recruitment.domain.ResultTemplateType;
import com.example.cau_likelion_spring.recruitment.domain.Round;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResultTemplateRepository extends JpaRepository<ResultTemplate, Long> {

    Optional<ResultTemplate> findByRoundAndType(Round round, ResultTemplateType type);

    /** 해당 템플릿을 저장한 적이 있는지 (기본 문구만 있는 상태는 작성 전으로 본다) */
    boolean existsByRoundAndType(Round round, ResultTemplateType type);
}

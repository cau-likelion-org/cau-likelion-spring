package com.example.cau_likelion_spring.recruitment.repository;

import com.example.cau_likelion_spring.recruitment.domain.Round;
import com.example.cau_likelion_spring.recruitment.domain.RoundPart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoundPartRepository extends JpaRepository<RoundPart, Long> {

    /** 해당 회차의 모집 파트 (생성 순) */
    List<RoundPart> findAllByRoundOrderByIdAsc(Round round);

    /** 해당 회차에서 특정 기수로 등록된 모집 파트 */
    List<RoundPart> findAllByRoundAndGeneration(Round round, Integer generation);
}

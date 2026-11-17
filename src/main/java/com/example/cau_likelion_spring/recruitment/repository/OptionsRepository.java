package com.example.cau_likelion_spring.recruitment.repository;

import com.example.cau_likelion_spring.recruitment.domain.Options;
import com.example.cau_likelion_spring.recruitment.domain.RoundPart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OptionsRepository extends JpaRepository<Options, Long> {

    /** 해당 파트의 객관식 보기 (만든 순) */
    List<Options> findAllByRoundPartOrderByIdAsc(RoundPart roundPart);
}

package com.example.cau_likelion_spring.recruitment.repository;

import com.example.cau_likelion_spring.recruitment.domain.InterviewOption;
import com.example.cau_likelion_spring.recruitment.domain.RoundPart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InterviewOptionRepository extends JpaRepository<InterviewOption, Long> {

    /** 해당 파트의 객관식 문항 내용 */
    List<InterviewOption> findAllByRoundPart(RoundPart roundPart);
}

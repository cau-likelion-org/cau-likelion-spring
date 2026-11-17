package com.example.cau_likelion_spring.recruitment.repository;

import com.example.cau_likelion_spring.recruitment.domain.InterviewQuestion;
import com.example.cau_likelion_spring.recruitment.domain.RoundPart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InterviewQuestionRepository extends JpaRepository<InterviewQuestion, Long> {

    /** 해당 파트의 2단계 문항 (만든 순) */
    List<InterviewQuestion> findAllByRoundPartOrderByIdAsc(RoundPart roundPart);
}

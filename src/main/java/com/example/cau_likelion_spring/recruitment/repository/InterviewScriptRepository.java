package com.example.cau_likelion_spring.recruitment.repository;

import com.example.cau_likelion_spring.recruitment.domain.InterviewScript;
import com.example.cau_likelion_spring.recruitment.domain.RoundPart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InterviewScriptRepository extends JpaRepository<InterviewScript, Long> {

    /** 해당 파트의 장문·링크·파일 문항 내용 */
    List<InterviewScript> findAllByRoundPart(RoundPart roundPart);
}

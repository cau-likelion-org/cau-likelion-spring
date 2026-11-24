package com.example.cau_likelion_spring.recruitment.repository;

import com.example.cau_likelion_spring.recruitment.domain.Options;
import com.example.cau_likelion_spring.recruitment.domain.InterviewQuestion;
import com.example.cau_likelion_spring.recruitment.domain.RoundPart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OptionsRepository extends JpaRepository<Options, Long> {

    /** 해당 파트의 객관식 보기 (만든 순) */
    List<Options> findAllByRoundPartOrderByIdAsc(RoundPart roundPart);

    /** 해당 문항의 객관식 보기 삭제 (백오피스 문항 수정·삭제 시 선택지 교체용) */
    @Modifying(flushAutomatically = true)
    @Query("delete from Options e where e.interviewQuestion = :interviewQuestion")
    void deleteAllByInterviewQuestion(InterviewQuestion interviewQuestion);
}

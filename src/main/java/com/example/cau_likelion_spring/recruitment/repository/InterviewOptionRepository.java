package com.example.cau_likelion_spring.recruitment.repository;

import com.example.cau_likelion_spring.recruitment.domain.InterviewOption;
import com.example.cau_likelion_spring.recruitment.domain.InterviewQuestion;
import com.example.cau_likelion_spring.recruitment.domain.RoundPart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface InterviewOptionRepository extends JpaRepository<InterviewOption, Long> {

    /** 해당 파트의 객관식 문항 내용 */
    List<InterviewOption> findAllByRoundPart(RoundPart roundPart);

    /** 해당 문항의 객관식 내용 삭제 (백오피스 문항 수정·삭제 시 내용 교체용, 선택지를 먼저 지워야 함) */
    @Modifying(flushAutomatically = true)
    @Query("delete from InterviewOption e where e.interviewQuestion = :interviewQuestion")
    void deleteAllByInterviewQuestion(InterviewQuestion interviewQuestion);
}

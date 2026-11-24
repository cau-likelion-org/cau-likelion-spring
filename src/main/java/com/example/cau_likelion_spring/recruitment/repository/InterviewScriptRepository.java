package com.example.cau_likelion_spring.recruitment.repository;

import com.example.cau_likelion_spring.recruitment.domain.InterviewScript;
import com.example.cau_likelion_spring.recruitment.domain.InterviewQuestion;
import com.example.cau_likelion_spring.recruitment.domain.RoundPart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface InterviewScriptRepository extends JpaRepository<InterviewScript, Long> {

    /** 해당 파트의 장문·링크·파일 문항 내용 */
    List<InterviewScript> findAllByRoundPart(RoundPart roundPart);

    /** 해당 문항의 장문 내용 삭제 (백오피스 문항 수정·삭제 시 내용 교체용) */
    @Modifying(flushAutomatically = true)
    @Query("delete from InterviewScript e where e.interviewQuestion = :interviewQuestion")
    void deleteAllByInterviewQuestion(InterviewQuestion interviewQuestion);
}

package com.example.cau_likelion_spring.recruitment.repository;

import com.example.cau_likelion_spring.recruitment.domain.InterviewQuestion;
import com.example.cau_likelion_spring.recruitment.domain.RoundPart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface InterviewQuestionRepository extends JpaRepository<InterviewQuestion, Long> {

    /** 해당 파트의 2단계 문항 (만든 순) */
    List<InterviewQuestion> findAllByRoundPartOrderByIdAsc(RoundPart roundPart);

    /** 해당 파트의 마지막 노출 순서 (문항이 없으면 0). 새 문항은 이 다음 순서로 맨 뒤에 붙는다 */
    @Query("select coalesce(max(e.sequence), 0) from InterviewQuestion e where e.roundPart = :roundPart")
    int findMaxSequence(RoundPart roundPart);
}

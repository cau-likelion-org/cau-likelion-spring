package com.example.cau_likelion_spring.recruitment.repository;

import com.example.cau_likelion_spring.recruitment.domain.InterviewTime;
import com.example.cau_likelion_spring.recruitment.domain.Round;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InterviewTimeRepository extends JpaRepository<InterviewTime, Long> {

    /** 해당 회차의 면접 가능 시간을 일시 오름차순으로 */
    List<InterviewTime> findAllByRoundOrderByInterviewDateAsc(Round round);
}

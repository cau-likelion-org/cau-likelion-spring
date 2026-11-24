package com.example.cau_likelion_spring.recruitment.repository;

import com.example.cau_likelion_spring.recruitment.domain.Applicant;
import com.example.cau_likelion_spring.recruitment.domain.ApplicantInterviewTime;
import com.example.cau_likelion_spring.recruitment.domain.Round;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicantInterviewTimeRepository extends JpaRepository<ApplicantInterviewTime, Long> {

    /** 지원자가 해당 회차에서 고른 면접 가능 시간 (면접 시간을 함께 조회) */
    @EntityGraph(attributePaths = "interviewTime")
    List<ApplicantInterviewTime> findAllByApplicantAndInterviewTime_Round(Applicant applicant, Round round);
}

package com.example.cau_likelion_spring.recruitment.repository;

import com.example.cau_likelion_spring.recruitment.domain.Applicant;
import com.example.cau_likelion_spring.recruitment.domain.Application;
import com.example.cau_likelion_spring.recruitment.domain.Round;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    /** 해당 회차에 같은 학번으로 제출된 지원서가 있는지 (회차는 roundPart.round 기준) */
    boolean existsByRoundPart_RoundAndStudentNumber(Round round, String studentNumber);

    /** 이 지원자가 해당 회차에 이미 제출했는지 */
    boolean existsByApplicantAndRoundPart_Round(Applicant applicant, Round round);

    /** 이 지원자가 해당 회차에 제출한 지원서 */
    Optional<Application> findByApplicantAndRoundPart_Round(Applicant applicant, Round round);
}

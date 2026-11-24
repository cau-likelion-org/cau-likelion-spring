package com.example.cau_likelion_spring.recruitment.repository;

import com.example.cau_likelion_spring.recruitment.domain.Applicant;
import com.example.cau_likelion_spring.recruitment.domain.Round;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApplicantRepository extends JpaRepository<Applicant, Long> {

    /** 해당 회차에 이 이메일로 가입한 계정 (회차마다 이메일당 1개) */
    Optional<Applicant> findByRoundAndEmail(Round round, String email);
}

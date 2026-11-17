package com.example.cau_likelion_spring.recruitment.repository;

import com.example.cau_likelion_spring.recruitment.domain.Applicant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicantRepository extends JpaRepository<Applicant, Long> {
}

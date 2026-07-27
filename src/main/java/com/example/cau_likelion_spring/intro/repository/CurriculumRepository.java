package com.example.cau_likelion_spring.intro.repository;

import com.example.cau_likelion_spring.intro.domain.Curriculum;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CurriculumRepository extends JpaRepository<Curriculum, Long> {
}

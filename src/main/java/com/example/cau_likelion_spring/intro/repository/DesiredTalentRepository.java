package com.example.cau_likelion_spring.intro.repository;

import com.example.cau_likelion_spring.intro.domain.DesiredTalent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DesiredTalentRepository extends JpaRepository<DesiredTalent, Long> {
}

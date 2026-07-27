package com.example.cau_likelion_spring.intro.repository;

import com.example.cau_likelion_spring.intro.domain.Faq;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FaqRepository extends JpaRepository<Faq, Long> {
}

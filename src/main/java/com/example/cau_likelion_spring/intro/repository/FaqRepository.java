package com.example.cau_likelion_spring.intro.repository;

import com.example.cau_likelion_spring.intro.domain.Faq;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FaqRepository extends JpaRepository<Faq, Long> {

    /** 현재 가장 큰 노출 순서 (FAQ가 없거나 모두 null이면 0) */
    @Query("select coalesce(max(f.displayOrder), 0) from Faq f")
    int findMaxDisplayOrder();
}

package com.example.cau_likelion_spring.gallery.history.repository;

import com.example.cau_likelion_spring.gallery.history.domain.History;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoryRepository extends JpaRepository<History, Long> {

    // 1. 기수 내림차순 -> 2. 시작일 내림차순 -> 3. 이름(title) 오름차순
    List<History> findAllByOrderByGeneration_NumberDescStartDateDescTitleAsc();
}

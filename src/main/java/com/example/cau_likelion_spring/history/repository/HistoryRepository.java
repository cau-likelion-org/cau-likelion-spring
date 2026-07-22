package com.example.cau_likelion_spring.history.repository;

import com.example.cau_likelion_spring.history.domain.History;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoryRepository extends JpaRepository<History, Long> {

    List<History> findAllByOrderByStartDateDesc();
}

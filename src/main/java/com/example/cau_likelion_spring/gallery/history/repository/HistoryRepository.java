package com.example.cau_likelion_spring.gallery.history.repository;

import com.example.cau_likelion_spring.gallery.history.domain.History;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoryRepository extends JpaRepository<History, Long> {

    List<History> findAllByOrderByStartDateDesc();
}

package com.example.cau_likelion_spring.history.repository;

import com.example.cau_likelion_spring.history.domain.History;
import com.example.cau_likelion_spring.history.domain.HistoryImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoryImageRepository extends JpaRepository<HistoryImage, Long> {

    List<HistoryImage> findByHistoryOrderBySortOrderAsc(History history);

    void deleteAllByHistory(History history);
}

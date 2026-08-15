package com.example.cau_likelion_spring.gallery.history.repository;

import com.example.cau_likelion_spring.gallery.history.domain.History;
import com.example.cau_likelion_spring.gallery.history.domain.HistoryImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoryImageRepository extends JpaRepository<HistoryImage, Long> {

    List<HistoryImage> findByHistoryOrderByIdAsc(History history);

    void deleteAllByHistory(History history);
}

package com.example.cau_likelion_spring.gallery.session.repository;

import com.example.cau_likelion_spring.gallery.session.domain.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, Long> {
    Optional<Session> findById(Long id);
    List<Session> findByPart_Name(String name);
    List<Session> findByPart_Generation_Number(int number);
    List<Session> findByPart_NameAndPart_Generation_Number(String partName, int number);
}

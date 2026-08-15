package com.example.cau_likelion_spring.gallery.session.repository;

import com.example.cau_likelion_spring.gallery.session.domain.Session;
import com.example.cau_likelion_spring.gallery.session.domain.SessionImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SessionImageRepository extends JpaRepository<SessionImage, Long> {

    List<SessionImage> findAllBySession(Session session);

    void deleteAllBySession(Session session);
}

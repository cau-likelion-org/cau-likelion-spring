package com.example.cau_likelion_spring.session.repository;

import com.example.cau_likelion_spring.session.domain.Session;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRepository extends JpaRepository<Session, Long> {

}

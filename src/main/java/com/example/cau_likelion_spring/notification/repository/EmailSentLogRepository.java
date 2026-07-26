package com.example.cau_likelion_spring.notification.repository;

import com.example.cau_likelion_spring.notification.domain.EmailSentLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailSentLogRepository extends JpaRepository<EmailSentLog, Long> {
}

package com.example.cau_likelion_spring.notification.repository;

import com.example.cau_likelion_spring.notification.domain.EmailSentLog;
import com.example.cau_likelion_spring.notification.domain.EmailSentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface EmailSentLogRepository extends JpaRepository<EmailSentLog, Long> {

    List<EmailSentLog> findAllByStatusAndRecruitmentText_ScheduledSendAtBefore(
            EmailSentStatus status, LocalDateTime dateTime);
}

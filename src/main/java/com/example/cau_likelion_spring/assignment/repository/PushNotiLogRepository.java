package com.example.cau_likelion_spring.assignment.repository;

import com.example.cau_likelion_spring.assignment.domain.Assignment;
import com.example.cau_likelion_spring.assignment.domain.PushNotiLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PushNotiLogRepository extends JpaRepository<PushNotiLog, Long> {

    void deleteAllByAssignmentSubmit_Assignment(Assignment assignment);
}

package com.example.cau_likelion_spring.assignment.repository;

import com.example.cau_likelion_spring.assignment.domain.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
}

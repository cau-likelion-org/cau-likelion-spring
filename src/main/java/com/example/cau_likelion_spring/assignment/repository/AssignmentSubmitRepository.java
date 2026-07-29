package com.example.cau_likelion_spring.assignment.repository;

import com.example.cau_likelion_spring.assignment.domain.Assignment;
import com.example.cau_likelion_spring.assignment.domain.AssignmentSubmit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssignmentSubmitRepository extends JpaRepository<AssignmentSubmit, Long> {

    void deleteAllByAssignment(Assignment assignment);
}

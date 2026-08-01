package com.example.cau_likelion_spring.assignment.repository;

import com.example.cau_likelion_spring.assignment.domain.Assignment;
import com.example.cau_likelion_spring.assignment.domain.AssignmentSubmit;
import com.example.cau_likelion_spring.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AssignmentSubmitRepository extends JpaRepository<AssignmentSubmit, Long> {

    void deleteAllByAssignment(Assignment assignment);

    Optional<AssignmentSubmit> findFirstByAssignmentAndSubmitMemberOrderByCreatedAtDesc(
            Assignment assignment, Member submitMember);
}

package com.example.cau_likelion_spring.assignment.repository;

import com.example.cau_likelion_spring.assignment.domain.Assignment;
import com.example.cau_likelion_spring.assignment.domain.AssignmentSubmit;
import com.example.cau_likelion_spring.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssignmentSubmitRepository extends JpaRepository<AssignmentSubmit, Long> {

    void deleteAllByAssignment(Assignment assignment);

    Optional<AssignmentSubmit> findFirstByAssignmentAndSubmitMemberOrderByCreatedAtDesc(
            Assignment assignment, Member submitMember);

    /** 아기사자 본인의 제출 이력 전체 (최신순) */
    List<AssignmentSubmit> findAllByAssignmentAndSubmitMemberOrderByCreatedAtDesc(
            Assignment assignment, Member submitMember);

    /** 운영진이 파트원 전체의 제출 이력을 한 번에 조회할 때 사용 (이후 멤버별로 그룹핑해서 최신 것만 추림) */
    List<AssignmentSubmit> findAllByAssignmentAndSubmitMember_IdInOrderByCreatedAtDesc(
            Assignment assignment, List<Long> memberIds);

    /** 아기사자 본인의 여러 과제에 대한 제출 이력을 한 번에 조회할 때 사용 (이후 과제별로 그룹핑해서 최신 것만 추림) */
    List<AssignmentSubmit> findAllByAssignment_IdInAndSubmitMemberOrderByCreatedAtDesc(
            List<Long> assignmentIds, Member submitMember);
}

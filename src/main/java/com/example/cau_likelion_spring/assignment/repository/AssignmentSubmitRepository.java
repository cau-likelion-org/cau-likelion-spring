package com.example.cau_likelion_spring.assignment.repository;

import com.example.cau_likelion_spring.assignment.domain.Assignment;
import com.example.cau_likelion_spring.assignment.domain.AssignmentSubmit;
import com.example.cau_likelion_spring.assignment.domain.AssignmentSubmitStatus;
import com.example.cau_likelion_spring.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AssignmentSubmitRepository extends JpaRepository<AssignmentSubmit, Long> {

    void deleteAllByAssignment(Assignment assignment);

    void deleteAllBySubmitMember_Id(Long memberId);

    /** 회원 삭제 시 그 사람이 평가한(reviewMember) 다른 사람의 제출은 지우지 않고, 평가자 참조만 비운다 */
    @Modifying
    @Query("UPDATE AssignmentSubmit s SET s.reviewMember = NULL WHERE s.reviewMember.id = :memberId")
    void clearReviewMemberById(@Param("memberId") Long memberId);

    /**
     * 평가(승인/반려) 결과를 벌크 UPDATE로 반영한다.
     * 평가 관련 컬럼만 SET하고 updatedAt은 건드리지 않아, BaseTimeEntity의 JPA auditing(@LastModifiedDate)이
     * 엔티티 저장 시 자동으로 updatedAt을 갱신하는 것을 우회한다 - updatedAt은 "제출 시각"이므로 평가로 바뀌면 안 된다.
     */
    @Modifying
    @Query("UPDATE AssignmentSubmit s SET s.status = :status, s.reviewMember = :reviewMember, " +
            "s.reviewerName = :reviewerName, s.approvalDate = :approvalDate, s.rejectionReason = :rejectionReason " +
            "WHERE s.id = :id")
    void applyEvaluation(@Param("id") Long id, @Param("status") AssignmentSubmitStatus status,
                          @Param("reviewMember") Member reviewMember, @Param("reviewerName") String reviewerName,
                          @Param("approvalDate") LocalDateTime approvalDate, @Param("rejectionReason") String rejectionReason);

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

    /** 운영진이 본인 파트의 여러 과제에 대한 제출 현황을 한 번에 집계할 때 사용 (이후 과제별·멤버별로 그룹핑해서 최신 것만 추림) */
    List<AssignmentSubmit> findAllByAssignment_IdInOrderByCreatedAtDesc(List<Long> assignmentIds);
}

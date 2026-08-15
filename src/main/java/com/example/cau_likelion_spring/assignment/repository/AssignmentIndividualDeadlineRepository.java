package com.example.cau_likelion_spring.assignment.repository;

import com.example.cau_likelion_spring.assignment.domain.Assignment;
import com.example.cau_likelion_spring.assignment.domain.AssignmentIndividualDeadline;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssignmentIndividualDeadlineRepository extends JpaRepository<AssignmentIndividualDeadline, Long> {

    Optional<AssignmentIndividualDeadline> findByAssignment_IdAndMember_Id(Long assignmentId, Long memberId);

    void deleteAllByAssignment(Assignment assignment);

    void deleteAllByMember_Id(Long memberId);

    /** 여러 과제에 걸친 개별 마감일을 한 번에 조회할 때 사용 (이후 과제별·멤버별로 그룹핑) */
    List<AssignmentIndividualDeadline> findAllByAssignment_IdIn(List<Long> assignmentIds);

    /** 아기사자 본인의 여러 과제에 대한 개별 마감일을 한 번에 조회할 때 사용 (이후 과제별로 그룹핑) */
    List<AssignmentIndividualDeadline> findAllByAssignment_IdInAndMember_Id(List<Long> assignmentIds, Long memberId);

    /** 마감일 변경 API에서 선택된 멤버들의 기존 오버라이드를 한 번에 조회할 때 사용 (있으면 갱신, 없으면 새로 생성) */
    List<AssignmentIndividualDeadline> findAllByAssignment_IdAndMember_IdIn(Long assignmentId, List<Long> memberIds);
}

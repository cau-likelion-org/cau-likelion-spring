package com.example.cau_likelion_spring.assignment.service;

import com.example.cau_likelion_spring.assignment.domain.Assignment;
import com.example.cau_likelion_spring.assignment.domain.AssignmentIndividualDeadline;
import com.example.cau_likelion_spring.assignment.domain.AssignmentSubmit;
import com.example.cau_likelion_spring.assignment.domain.AssignmentSubmitDisplayStatus;
import com.example.cau_likelion_spring.assignment.domain.AssignmentSubmitDisplayStatusCalculator;
import com.example.cau_likelion_spring.assignment.dto.AssignmentSummaryResponse;
import com.example.cau_likelion_spring.assignment.dto.AssignmentWeekGroupResponse;
import com.example.cau_likelion_spring.assignment.exception.BabyLionPartNotAssignedException;
import com.example.cau_likelion_spring.assignment.repository.AssignmentIndividualDeadlineRepository;
import com.example.cau_likelion_spring.assignment.repository.AssignmentRepository;
import com.example.cau_likelion_spring.assignment.repository.AssignmentSubmitRepository;
import com.example.cau_likelion_spring.member.domain.Member;
import com.example.cau_likelion_spring.member.exception.MemberNotFoundException;
import com.example.cau_likelion_spring.member.repository.MemberRepository;
import com.example.cau_likelion_spring.organization.domain.Part;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 아기사자 본인의 과제 목록(주차별 그룹) 조회 서비스.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssignmentBabyLionService {

    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmitRepository assignmentSubmitRepository;
    private final AssignmentIndividualDeadlineRepository assignmentIndividualDeadlineRepository;
    private final MemberRepository memberRepository;

    /**
     * week를 지정하면 해당 주차만, 지정하지 않으면 전체 주차를 주차 오름차순으로 묶어서 반환한다.
     * 같은 주차라도 과제마다 마감기한이 다를 수 있어 각 과제 생성 시점의 마감기한을 그대로 보여준다.
     */
    @PreAuthorize("hasRole('BABY_LION')")
    public List<AssignmentWeekGroupResponse> getMyAssignments(Long memberId, Integer week) {
        Member member = getMember(memberId);
        Part part = getMemberPart(member);

        List<Assignment> assignments = (week == null)
                ? assignmentRepository.findAllByPart_IdOrderByWeekAscEndDateAsc(part.getId())
                : assignmentRepository.findAllByPart_IdAndWeekOrderByEndDateAsc(part.getId(), week);
        if (assignments.isEmpty()) {
            return List.of();
        }

        Map<Long, AssignmentSubmit> latestSubmitByAssignmentId = findLatestSubmitsByAssignmentId(assignments, member);
        Map<Long, LocalDateTime> individualDeadlineByAssignmentId = findIndividualDeadlinesByAssignmentId(assignments, member);

        Map<Integer, List<Assignment>> assignmentsByWeek = assignments.stream()
                .collect(Collectors.groupingBy(Assignment::getWeek, LinkedHashMap::new, Collectors.toList()));

        return assignmentsByWeek.entrySet().stream()
                .map(entry -> toWeekGroup(entry.getKey(), entry.getValue(), latestSubmitByAssignmentId, individualDeadlineByAssignmentId))
                .toList();
    }

    private Map<Long, AssignmentSubmit> findLatestSubmitsByAssignmentId(List<Assignment> assignments, Member member) {
        List<Long> assignmentIds = assignments.stream().map(Assignment::getId).toList();

        return assignmentSubmitRepository
                .findAllByAssignment_IdInAndSubmitMemberOrderByCreatedAtDesc(assignmentIds, member).stream()
                .collect(Collectors.toMap(submit -> submit.getAssignment().getId(), submit -> submit,
                        (firstByCreatedAtDesc, ignoredOlder) -> firstByCreatedAtDesc));
    }

    private Map<Long, LocalDateTime> findIndividualDeadlinesByAssignmentId(List<Assignment> assignments, Member member) {
        List<Long> assignmentIds = assignments.stream().map(Assignment::getId).toList();

        return assignmentIndividualDeadlineRepository
                .findAllByAssignment_IdInAndMember_Id(assignmentIds, member.getId()).stream()
                .collect(Collectors.toMap(deadline -> deadline.getAssignment().getId(), AssignmentIndividualDeadline::getDeadline));
    }

    private AssignmentWeekGroupResponse toWeekGroup(Integer week, List<Assignment> weekAssignments,
                                                      Map<Long, AssignmentSubmit> latestSubmitByAssignmentId,
                                                      Map<Long, LocalDateTime> individualDeadlineByAssignmentId) {
        List<AssignmentSummaryResponse> assignmentSummaries = weekAssignments.stream()
                .map(assignment -> toSummary(assignment, latestSubmitByAssignmentId.get(assignment.getId()),
                        individualDeadlineByAssignmentId.getOrDefault(assignment.getId(), assignment.getEndDate())))
                .toList();

        return new AssignmentWeekGroupResponse(week, assignmentSummaries);
    }

    private AssignmentSummaryResponse toSummary(Assignment assignment, AssignmentSubmit latest, LocalDateTime endDate) {
        AssignmentSubmitDisplayStatus status = AssignmentSubmitDisplayStatusCalculator.calculate(endDate, latest);
        LocalDateTime submittedAt = latest == null ? null : latest.getUpdatedAt();
        return new AssignmentSummaryResponse(assignment.getId(), assignment.getTitle(), endDate, status, submittedAt);
    }

    private Member getMember(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new MemberNotFoundException(id));
    }

    private Part getMemberPart(Member member) {
        Part part = member.getPart();
        if (part == null) {
            throw new BabyLionPartNotAssignedException(member.getId());
        }
        return part;
    }
}

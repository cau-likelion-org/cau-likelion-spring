package com.example.cau_likelion_spring.assignment.service;

import com.example.cau_likelion_spring.assignment.domain.Assignment;
import com.example.cau_likelion_spring.assignment.domain.AssignmentIndividualDeadline;
import com.example.cau_likelion_spring.assignment.domain.AssignmentSubmit;
import com.example.cau_likelion_spring.assignment.domain.AssignmentSubmitDisplayStatus;
import com.example.cau_likelion_spring.assignment.domain.AssignmentSubmitDisplayStatusCalculator;
import com.example.cau_likelion_spring.assignment.dto.AssignmentCreateRequest;
import com.example.cau_likelion_spring.assignment.dto.AssignmentIndividualDeadlineRequest;
import com.example.cau_likelion_spring.assignment.dto.AssignmentIndividualDeadlineResponse;
import com.example.cau_likelion_spring.assignment.dto.AssignmentResponse;
import com.example.cau_likelion_spring.assignment.dto.AssignmentStaffSummaryResponse;
import com.example.cau_likelion_spring.assignment.dto.AssignmentStaffWeekGroupResponse;
import com.example.cau_likelion_spring.assignment.dto.AssignmentUpdateRequest;
import com.example.cau_likelion_spring.assignment.exception.AssignmentMemberPartMismatchException;
import com.example.cau_likelion_spring.assignment.exception.AssignmentNotFoundException;
import com.example.cau_likelion_spring.assignment.exception.AssignmentPartMismatchException;
import com.example.cau_likelion_spring.assignment.exception.StaffPartNotAssignedException;
import com.example.cau_likelion_spring.assignment.repository.AssignmentIndividualDeadlineRepository;
import com.example.cau_likelion_spring.assignment.repository.AssignmentRepository;
import com.example.cau_likelion_spring.assignment.repository.AssignmentSubmitRepository;
import com.example.cau_likelion_spring.assignment.repository.PushNotiLogRepository;
import com.example.cau_likelion_spring.assignment.repository.SubmissionFileRepository;
import com.example.cau_likelion_spring.member.domain.Member;
import com.example.cau_likelion_spring.member.domain.MemberRole;
import com.example.cau_likelion_spring.member.exception.MemberNotFoundException;
import com.example.cau_likelion_spring.member.exception.PartNotFoundException;
import com.example.cau_likelion_spring.member.repository.MemberRepository;
import com.example.cau_likelion_spring.organization.domain.Part;
import com.example.cau_likelion_spring.organization.repository.PartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmitRepository assignmentSubmitRepository;
    private final AssignmentIndividualDeadlineRepository assignmentIndividualDeadlineRepository;
    private final SubmissionFileRepository submissionFileRepository;
    private final PushNotiLogRepository pushNotiLogRepository;
    private final MemberRepository memberRepository;
    private final PartRepository partRepository;

    /**
     * 한 주차에 개별 과제 1개 이상을 한 번에 생성한다 (생성 페이지에서 +로 여러 개를 모아 한 번에 저장하는 흐름).
     */
    @PreAuthorize("hasRole('STAFF')")
    @Transactional
    public List<AssignmentResponse> create(Long staffMemberId, AssignmentCreateRequest request) {
        Part part = getStaffPart(staffMemberId);

        List<Assignment> assignments = request.assignments().stream()
                .map(item -> Assignment.builder()
                        .part(part)
                        .week(request.week())
                        .title(item.title())
                        .detail(item.detail())
                        .endDate(item.endDate())
                        .type(item.type())
                        .build())
                .toList();

        return assignmentRepository.saveAll(assignments).stream()
                .map(AssignmentResponse::of)
                .toList();
    }

    @PreAuthorize("hasRole('STAFF')")
    @Transactional
    public AssignmentResponse update(Long staffMemberId, Long assignmentId, AssignmentUpdateRequest request) {
        Assignment assignment = getOwnedAssignment(staffMemberId, assignmentId);

        assignment.update(request.title(), request.detail(), request.endDate(), request.type());

        return AssignmentResponse.of(assignment);
    }

    /**
     * 과제 삭제. 이전에 제출된 과제(AssignmentSubmit)와 그에 딸린 파일/알림 이력도 함께 삭제된다.
     * (수정과 달리 삭제는 제출 이력을 보존하지 않음)
     */
    @PreAuthorize("hasRole('STAFF')")
    @Transactional
    public void delete(Long staffMemberId, Long assignmentId) {
        Assignment assignment = getOwnedAssignment(staffMemberId, assignmentId);

        pushNotiLogRepository.deleteAllByAssignmentSubmit_Assignment(assignment);
        submissionFileRepository.deleteAllByAssignmentSubmit_Assignment(assignment);
        assignmentSubmitRepository.deleteAllByAssignment(assignment);
        assignmentRepository.delete(assignment);
    }

    /**
     * 선택한 아기사자(들)에게 이 과제의 개별 마감일을 부여/변경한다. 이후 이 아기사자들은 제출 가능 여부와
     * 정시/지각 판정 모두 Assignment.endDate 대신 이 개별 마감일 기준으로 계산된다. 이미 개별 마감일이
     * 있으면 값을 덮어쓰고, 없으면 새로 생성한다 (아기사자 1명당 과제 1개에 최대 1건).
     */
    @PreAuthorize("hasRole('STAFF')")
    @Transactional
    public List<AssignmentIndividualDeadlineResponse> updateIndividualDeadlines(Long staffMemberId, Long assignmentId,
                                                                                  AssignmentIndividualDeadlineRequest request) {
        Assignment assignment = getOwnedAssignment(staffMemberId, assignmentId);

        List<Member> members = memberRepository.findAllById(request.memberIds());
        if (members.size() != request.memberIds().size()) {
            List<Long> foundIds = members.stream().map(Member::getId).toList();
            Long missingId = request.memberIds().stream().filter(id -> !foundIds.contains(id)).findFirst().orElseThrow();
            throw new MemberNotFoundException(missingId);
        }
        for (Member member : members) {
            if (member.getPart() == null || !member.getPart().getId().equals(assignment.getPart().getId())) {
                throw new AssignmentMemberPartMismatchException(member.getId());
            }
        }

        Map<Long, AssignmentIndividualDeadline> existingByMemberId = assignmentIndividualDeadlineRepository
                .findAllByAssignment_IdAndMember_IdIn(assignmentId, request.memberIds()).stream()
                .collect(Collectors.toMap(deadline -> deadline.getMember().getId(), deadline -> deadline));

        return members.stream()
                .map(member -> {
                    AssignmentIndividualDeadline deadline = existingByMemberId.get(member.getId());
                    if (deadline == null) {
                        deadline = assignmentIndividualDeadlineRepository.save(AssignmentIndividualDeadline.builder()
                                .assignment(assignment)
                                .member(member)
                                .deadline(request.deadline())
                                .build());
                    } else {
                        deadline.updateDeadline(request.deadline());
                    }
                    return AssignmentIndividualDeadlineResponse.of(deadline);
                })
                .toList();
    }

    /**
     * 운영진이 보는 본인 파트 과제 목록(주차별). 과제마다 파트원 전체를 대상으로 최신 제출 기준
     * 제출전/미제출/승인대기/지각제출/승인완료 인원 수를 함께 집계해서 보여준다.
     */
    @PreAuthorize("hasRole('STAFF')")
    public List<AssignmentStaffWeekGroupResponse> getMyAssignmentsForStaff(Long staffMemberId) {
        Part part = getStaffPart(staffMemberId);
        return getAssignmentsByPart(part);
    }

    /**
     * 회장이 보는 특정 파트 과제 목록(주차별). 파트를 파라미터로 직접 지정한다는 점을 제외하면
     * {@link #getMyAssignmentsForStaff}와 동일한 집계 로직을 사용한다.
     */
    @PreAuthorize("hasRole('PRESIDENT')")
    public List<AssignmentStaffWeekGroupResponse> getAssignmentsForPresident(Long partId) {
        Part part = getPart(partId);
        return getAssignmentsByPart(part);
    }

    private List<AssignmentStaffWeekGroupResponse> getAssignmentsByPart(Part part) {
        List<Assignment> assignments = assignmentRepository.findAllByPart_IdOrderByWeekAscEndDateAsc(part.getId());
        if (assignments.isEmpty()) {
            return List.of();
        }

        List<Member> babyLions = memberRepository.findByPart_IdAndRole(part.getId(), MemberRole.BABY_LION);
        Map<Long, Map<Long, AssignmentSubmit>> latestSubmitByAssignmentIdThenMemberId =
                findLatestSubmitsByAssignmentIdThenMemberId(assignments);
        Map<Long, Map<Long, LocalDateTime>> individualDeadlineByAssignmentIdThenMemberId =
                findIndividualDeadlinesByAssignmentIdThenMemberId(assignments);

        Map<Integer, List<Assignment>> assignmentsByWeek = assignments.stream()
                .collect(Collectors.groupingBy(Assignment::getWeek, LinkedHashMap::new, Collectors.toList()));

        return assignmentsByWeek.entrySet().stream()
                .map(entry -> toStaffWeekGroup(entry.getKey(), entry.getValue(), babyLions,
                        latestSubmitByAssignmentIdThenMemberId, individualDeadlineByAssignmentIdThenMemberId))
                .toList();
    }

    private Map<Long, Map<Long, AssignmentSubmit>> findLatestSubmitsByAssignmentIdThenMemberId(List<Assignment> assignments) {
        List<Long> assignmentIds = assignments.stream().map(Assignment::getId).toList();

        return assignmentSubmitRepository.findAllByAssignment_IdInOrderByCreatedAtDesc(assignmentIds).stream()
                .collect(Collectors.groupingBy(submit -> submit.getAssignment().getId(),
                        Collectors.toMap(submit -> submit.getSubmitMember().getId(), submit -> submit,
                                (firstByCreatedAtDesc, ignoredOlder) -> firstByCreatedAtDesc)));
    }

    private Map<Long, Map<Long, LocalDateTime>> findIndividualDeadlinesByAssignmentIdThenMemberId(List<Assignment> assignments) {
        List<Long> assignmentIds = assignments.stream().map(Assignment::getId).toList();

        return assignmentIndividualDeadlineRepository.findAllByAssignment_IdIn(assignmentIds).stream()
                .collect(Collectors.groupingBy(deadline -> deadline.getAssignment().getId(),
                        Collectors.toMap(deadline -> deadline.getMember().getId(), AssignmentIndividualDeadline::getDeadline)));
    }

    private AssignmentStaffWeekGroupResponse toStaffWeekGroup(Integer week, List<Assignment> weekAssignments, List<Member> babyLions,
                                                                Map<Long, Map<Long, AssignmentSubmit>> latestSubmitByAssignmentIdThenMemberId,
                                                                Map<Long, Map<Long, LocalDateTime>> individualDeadlineByAssignmentIdThenMemberId) {
        List<AssignmentStaffSummaryResponse> assignmentSummaries = weekAssignments.stream()
                .map(assignment -> toStaffSummary(assignment, babyLions,
                        latestSubmitByAssignmentIdThenMemberId.getOrDefault(assignment.getId(), Map.of()),
                        individualDeadlineByAssignmentIdThenMemberId.getOrDefault(assignment.getId(), Map.of())))
                .toList();

        return new AssignmentStaffWeekGroupResponse(week, assignmentSummaries);
    }

    private AssignmentStaffSummaryResponse toStaffSummary(Assignment assignment, List<Member> babyLions,
                                                            Map<Long, AssignmentSubmit> latestSubmitByMemberId,
                                                            Map<Long, LocalDateTime> individualDeadlineByMemberId) {
        int beforeSubmissionCount = 0;
        int missedCount = 0;
        int pendingReviewCount = 0;
        int lateSubmittedCount = 0;
        int approvedCount = 0;

        for (Member babyLion : babyLions) {
            AssignmentSubmit latest = latestSubmitByMemberId.get(babyLion.getId());
            LocalDateTime endDate = individualDeadlineByMemberId.getOrDefault(babyLion.getId(), assignment.getEndDate());
            AssignmentSubmitDisplayStatus status = AssignmentSubmitDisplayStatusCalculator.calculate(endDate, latest);
            switch (status) {
                case BEFORE_SUBMISSION -> beforeSubmissionCount++;
                case MISSED -> missedCount++;
                case PENDING_REVIEW -> pendingReviewCount++;
                case LATE_SUBMITTED -> lateSubmittedCount++;
                case APPROVED -> approvedCount++;
                default -> {
                }
            }
        }

        return new AssignmentStaffSummaryResponse(assignment.getId(), assignment.getTitle(), assignment.getEndDate(),
                beforeSubmissionCount, missedCount, pendingReviewCount, lateSubmittedCount, approvedCount);
    }

    private Assignment getOwnedAssignment(Long staffMemberId, Long assignmentId) {
        Part staffPart = getStaffPart(staffMemberId);
        Assignment assignment = getAssignment(assignmentId);

        if (!assignment.getPart().getId().equals(staffPart.getId())) {
            throw new AssignmentPartMismatchException(assignmentId);
        }

        return assignment;
    }

    private Assignment getAssignment(Long id) {
        return assignmentRepository.findById(id)
                .orElseThrow(() -> new AssignmentNotFoundException(id));
    }

    private Part getPart(Long partId) {
        return partRepository.findById(partId)
                .orElseThrow(() -> new PartNotFoundException(partId));
    }

    private Part getStaffPart(Long staffMemberId) {
        Member staff = memberRepository.findById(staffMemberId)
                .orElseThrow(() -> new MemberNotFoundException(staffMemberId));

        Part part = staff.getPart();
        if (part == null) {
            throw new StaffPartNotAssignedException(staffMemberId);
        }

        return part;
    }
}

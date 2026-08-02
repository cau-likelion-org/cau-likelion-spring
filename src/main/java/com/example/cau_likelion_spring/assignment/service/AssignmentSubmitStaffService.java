package com.example.cau_likelion_spring.assignment.service;

import com.example.cau_likelion_spring.assignment.domain.Assignment;
import com.example.cau_likelion_spring.assignment.domain.AssignmentSubmit;
import com.example.cau_likelion_spring.assignment.domain.AssignmentSubmitDisplayStatus;
import com.example.cau_likelion_spring.assignment.domain.AssignmentSubmitDisplayStatusCalculator;
import com.example.cau_likelion_spring.assignment.domain.AssignmentSubmitStatus;
import com.example.cau_likelion_spring.assignment.domain.SubmissionFile;
import com.example.cau_likelion_spring.assignment.dto.AssignmentMemberSubmissionResponse;
import com.example.cau_likelion_spring.assignment.dto.AssignmentStaffDetailResponse;
import com.example.cau_likelion_spring.assignment.dto.AssignmentStaffDetailWeekGroupResponse;
import com.example.cau_likelion_spring.assignment.dto.AssignmentSubmitEvaluateRequest;
import com.example.cau_likelion_spring.assignment.dto.AssignmentSubmitResponse;
import com.example.cau_likelion_spring.assignment.exception.AssignmentNotFoundException;
import com.example.cau_likelion_spring.assignment.exception.AssignmentPartMismatchException;
import com.example.cau_likelion_spring.assignment.exception.AssignmentSubmitNotFoundException;
import com.example.cau_likelion_spring.assignment.exception.InvalidSubmissionException;
import com.example.cau_likelion_spring.assignment.exception.StaffPartNotAssignedException;
import com.example.cau_likelion_spring.assignment.repository.AssignmentRepository;
import com.example.cau_likelion_spring.assignment.repository.AssignmentSubmitRepository;
import com.example.cau_likelion_spring.assignment.repository.SubmissionFileRepository;
import com.example.cau_likelion_spring.member.domain.Member;
import com.example.cau_likelion_spring.member.domain.MemberRole;
import com.example.cau_likelion_spring.member.exception.MemberNotFoundException;
import com.example.cau_likelion_spring.member.repository.MemberRepository;
import com.example.cau_likelion_spring.organization.domain.Part;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 운영진(STAFF)이 파트원의 과제 제출 현황을 조회하는 서비스.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssignmentSubmitStaffService {

    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmitRepository assignmentSubmitRepository;
    private final SubmissionFileRepository submissionFileRepository;
    private final MemberRepository memberRepository;

    /**
     * 운영진이 보는 파트원 전체의 제출 현황. 제출했다면 최종본만 보여주고,
     * 한 번도 제출하지 않은 파트원도 "제출전"/"미제출" 상태로 함께 노출된다.
     */
    @PreAuthorize("hasRole('STAFF')")
    public List<AssignmentMemberSubmissionResponse> getSubmissionsForStaff(Long staffMemberId, Long assignmentId) {
        Assignment assignment = getAssignment(assignmentId);
        Part staffPart = getStaffPart(staffMemberId);
        if (!assignment.getPart().getId().equals(staffPart.getId())) {
            throw new AssignmentPartMismatchException(assignmentId);
        }

        List<Member> babyLions = memberRepository.findByPart_IdAndRole(assignment.getPart().getId(), MemberRole.BABY_LION);
        if (babyLions.isEmpty()) {
            return List.of();
        }

        List<Long> memberIds = babyLions.stream().map(Member::getId).toList();
        List<AssignmentSubmit> allSubmits = assignmentSubmitRepository
                .findAllByAssignmentAndSubmitMember_IdInOrderByCreatedAtDesc(assignment, memberIds);

        Map<Long, List<AssignmentSubmit>> submitsByMemberId = allSubmits.stream()
                .collect(Collectors.groupingBy(submit -> submit.getSubmitMember().getId()));
        Map<Long, List<SubmissionFile>> filesBySubmitId = groupFilesBySubmitId(allSubmits);

        return babyLions.stream()
                .map(member -> {
                    List<AssignmentSubmit> submits = submitsByMemberId.getOrDefault(member.getId(), List.of());
                    AssignmentSubmit latest = submits.isEmpty() ? null : submits.get(0);
                    return toMemberSubmission(member, assignment, latest, filesBySubmitId);
                })
                .toList();
    }

    /**
     * 운영진이 보는 본인 파트 과제별 파트원 전체 제출 현황(주차별로 묶임). 화면에서 주차 → 개별 과제 → 아기사자별
     * 내역(이름/최종 제출 시각/제출물/상태/리뷰 운영진 이름) 순으로 펼쳐 보여주고, 평가 버튼에 필요한 submitId도 함께 내려준다.
     */
    @PreAuthorize("hasRole('STAFF')")
    public List<AssignmentStaffDetailWeekGroupResponse> getSubmissionStatusForStaff(Long staffMemberId) {
        Part part = getStaffPart(staffMemberId);

        List<Assignment> assignments = assignmentRepository.findAllByPart_IdOrderByWeekAscEndDateAsc(part.getId());
        if (assignments.isEmpty()) {
            return List.of();
        }

        List<Member> babyLions = memberRepository.findByPart_IdAndRole(part.getId(), MemberRole.BABY_LION);

        List<Long> assignmentIds = assignments.stream().map(Assignment::getId).toList();
        List<AssignmentSubmit> allSubmits = assignmentSubmitRepository.findAllByAssignment_IdInOrderByCreatedAtDesc(assignmentIds);

        Map<Long, Map<Long, AssignmentSubmit>> latestByAssignmentIdThenMemberId = allSubmits.stream()
                .collect(Collectors.groupingBy(submit -> submit.getAssignment().getId(),
                        Collectors.toMap(submit -> submit.getSubmitMember().getId(), submit -> submit,
                                (firstByCreatedAtDesc, ignoredOlder) -> firstByCreatedAtDesc)));
        Map<Long, List<SubmissionFile>> filesBySubmitId = groupFilesBySubmitId(latestByAssignmentIdThenMemberId.values().stream()
                .flatMap(byMemberId -> byMemberId.values().stream())
                .toList());

        Map<Integer, List<Assignment>> assignmentsByWeek = assignments.stream()
                .collect(Collectors.groupingBy(Assignment::getWeek, LinkedHashMap::new, Collectors.toList()));

        return assignmentsByWeek.entrySet().stream()
                .map(entry -> toStaffDetailWeekGroup(entry.getKey(), entry.getValue(), babyLions,
                        latestByAssignmentIdThenMemberId, filesBySubmitId))
                .toList();
    }

    private AssignmentStaffDetailWeekGroupResponse toStaffDetailWeekGroup(Integer week, List<Assignment> weekAssignments,
                                                                            List<Member> babyLions,
                                                                            Map<Long, Map<Long, AssignmentSubmit>> latestByAssignmentIdThenMemberId,
                                                                            Map<Long, List<SubmissionFile>> filesBySubmitId) {
        List<AssignmentStaffDetailResponse> assignmentDetails = weekAssignments.stream()
                .map(assignment -> toStaffDetail(assignment, babyLions,
                        latestByAssignmentIdThenMemberId.getOrDefault(assignment.getId(), Map.of()), filesBySubmitId))
                .toList();

        return new AssignmentStaffDetailWeekGroupResponse(week, assignmentDetails);
    }

    private AssignmentStaffDetailResponse toStaffDetail(Assignment assignment, List<Member> babyLions,
                                                          Map<Long, AssignmentSubmit> latestByMemberId,
                                                          Map<Long, List<SubmissionFile>> filesBySubmitId) {
        List<AssignmentMemberSubmissionResponse> submissions = babyLions.stream()
                .map(member -> toMemberSubmission(member, assignment, latestByMemberId.get(member.getId()), filesBySubmitId))
                .toList();

        return new AssignmentStaffDetailResponse(assignment.getId(), assignment.getTitle(), assignment.getEndDate(), submissions);
    }

    private AssignmentMemberSubmissionResponse toMemberSubmission(Member member, Assignment assignment, AssignmentSubmit latest,
                                                                    Map<Long, List<SubmissionFile>> filesBySubmitId) {
        AssignmentSubmitDisplayStatus displayStatus = AssignmentSubmitDisplayStatusCalculator.calculate(assignment, latest);
        AssignmentSubmitResponse latestResponse = latest == null ? null
                : AssignmentSubmitResponse.of(latest, filesBySubmitId.getOrDefault(latest.getId(), List.of()), displayStatus);
        return AssignmentMemberSubmissionResponse.of(member, displayStatus, latestResponse);
    }

    /**
     * 운영진이 제출을 승인/반려로 평가한다. 승인 시 제출 시각(createdAt)이 마감일 이전이면 '승인완료',
     * 이후면 '지각제출'로 표시되고(AssignmentSubmitDisplayStatusCalculator가 계산), 반려는 시점과 무관하게 '승인반려'로 표시된다.
     * 평가하면 reviewMember/approvalDate가 갱신된다.
     */
    @PreAuthorize("hasRole('STAFF')")
    @Transactional
    public AssignmentSubmitResponse evaluate(Long staffMemberId, Long assignmentId, Long submitId,
                                              AssignmentSubmitEvaluateRequest request) {
        Assignment assignment = getAssignment(assignmentId);
        Part staffPart = getStaffPart(staffMemberId);
        Member staff = getMember(staffMemberId);
        if (!assignment.getPart().getId().equals(staffPart.getId())) {
            throw new AssignmentPartMismatchException(assignmentId);
        }

        AssignmentSubmit submit = getSubmit(submitId);
        if (!submit.getAssignment().getId().equals(assignmentId)) {
            throw new AssignmentSubmitNotFoundException(submitId);
        }

        switch (request.status()) {
            case APPROVED -> submit.approve(staff);
            case REJECTED -> {
                if (!StringUtils.hasText(request.rejectionReason())) {
                    throw new InvalidSubmissionException("반려 사유를 입력해주세요.");
                }
                submit.reject(staff, request.rejectionReason());
            }
            case PENDING -> throw new InvalidSubmissionException("평가 상태는 APPROVED 또는 REJECTED만 가능합니다.");
        }

        List<SubmissionFile> files = submissionFileRepository.findAllByAssignmentSubmit(submit);
        AssignmentSubmitDisplayStatus displayStatus = AssignmentSubmitDisplayStatusCalculator.calculate(assignment, submit);
        return AssignmentSubmitResponse.of(submit, files, displayStatus);
    }

    private AssignmentSubmit getSubmit(Long submitId) {
        return assignmentSubmitRepository.findById(submitId)
                .orElseThrow(() -> new AssignmentSubmitNotFoundException(submitId));
    }

    private Map<Long, List<SubmissionFile>> groupFilesBySubmitId(List<AssignmentSubmit> submits) {
        return submissionFileRepository.findAllByAssignmentSubmitIn(submits).stream()
                .collect(Collectors.groupingBy(file -> file.getAssignmentSubmit().getId()));
    }

    private Assignment getAssignment(Long id) {
        return assignmentRepository.findById(id)
                .orElseThrow(() -> new AssignmentNotFoundException(id));
    }

    private Part getStaffPart(Long staffMemberId) {
        Member staff = getMember(staffMemberId);
        Part part = staff.getPart();
        if (part == null) {
            throw new StaffPartNotAssignedException(staffMemberId);
        }
        return part;
    }

    private Member getMember(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new MemberNotFoundException(id));
    }
}

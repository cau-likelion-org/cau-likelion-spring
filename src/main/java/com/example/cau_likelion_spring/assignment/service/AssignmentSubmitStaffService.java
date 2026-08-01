package com.example.cau_likelion_spring.assignment.service;

import com.example.cau_likelion_spring.assignment.domain.Assignment;
import com.example.cau_likelion_spring.assignment.domain.AssignmentSubmit;
import com.example.cau_likelion_spring.assignment.domain.AssignmentSubmitDisplayStatus;
import com.example.cau_likelion_spring.assignment.domain.AssignmentSubmitDisplayStatusCalculator;
import com.example.cau_likelion_spring.assignment.domain.SubmissionFile;
import com.example.cau_likelion_spring.assignment.dto.AssignmentMemberSubmissionResponse;
import com.example.cau_likelion_spring.assignment.dto.AssignmentSubmitResponse;
import com.example.cau_likelion_spring.assignment.exception.AssignmentNotFoundException;
import com.example.cau_likelion_spring.assignment.exception.AssignmentPartMismatchException;
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
                    AssignmentSubmitDisplayStatus displayStatus = AssignmentSubmitDisplayStatusCalculator.calculate(assignment, latest);
                    AssignmentSubmitResponse latestResponse = latest == null ? null
                            : AssignmentSubmitResponse.of(latest,
                                    filesBySubmitId.getOrDefault(latest.getId(), List.of()), displayStatus);
                    return AssignmentMemberSubmissionResponse.of(member, displayStatus, latestResponse);
                })
                .toList();
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

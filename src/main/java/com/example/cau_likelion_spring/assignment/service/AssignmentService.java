package com.example.cau_likelion_spring.assignment.service;

import com.example.cau_likelion_spring.assignment.domain.Assignment;
import com.example.cau_likelion_spring.assignment.dto.AssignmentCreateRequest;
import com.example.cau_likelion_spring.assignment.dto.AssignmentResponse;
import com.example.cau_likelion_spring.assignment.dto.AssignmentUpdateRequest;
import com.example.cau_likelion_spring.assignment.exception.AssignmentNotFoundException;
import com.example.cau_likelion_spring.assignment.exception.AssignmentPartMismatchException;
import com.example.cau_likelion_spring.assignment.exception.StaffPartNotAssignedException;
import com.example.cau_likelion_spring.assignment.repository.AssignmentRepository;
import com.example.cau_likelion_spring.assignment.repository.AssignmentSubmitRepository;
import com.example.cau_likelion_spring.assignment.repository.PushNotiLogRepository;
import com.example.cau_likelion_spring.assignment.repository.SubmissionFileRepository;
import com.example.cau_likelion_spring.member.domain.Member;
import com.example.cau_likelion_spring.member.exception.MemberNotFoundException;
import com.example.cau_likelion_spring.member.repository.MemberRepository;
import com.example.cau_likelion_spring.organization.domain.Part;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmitRepository assignmentSubmitRepository;
    private final SubmissionFileRepository submissionFileRepository;
    private final PushNotiLogRepository pushNotiLogRepository;
    private final MemberRepository memberRepository;

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

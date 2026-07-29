package com.example.cau_likelion_spring.assignment.service;

import com.example.cau_likelion_spring.assignment.domain.Assignment;
import com.example.cau_likelion_spring.assignment.dto.AssignmentRequest;
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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmitRepository assignmentSubmitRepository;
    private final SubmissionFileRepository submissionFileRepository;
    private final PushNotiLogRepository pushNotiLogRepository;
    private final MemberRepository memberRepository;

    @PreAuthorize("hasRole('STAFF')")
    @Transactional
    public AssignmentResponse create(Long staffMemberId, AssignmentRequest request) {
        Part part = getStaffPart(staffMemberId);

        Assignment assignment = assignmentRepository.save(Assignment.builder()
                .part(part)
                .week(request.week())
                .title(request.title())
                .detail(request.detail())
                .endDate(request.endDate())
                .type(request.type())
                .build());

        return AssignmentResponse.of(assignment);
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

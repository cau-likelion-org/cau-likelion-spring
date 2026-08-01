package com.example.cau_likelion_spring.assignment.service;

import com.example.cau_likelion_spring.assignment.domain.Assignment;
import com.example.cau_likelion_spring.assignment.domain.AssignmentSubmit;
import com.example.cau_likelion_spring.assignment.domain.AssignmentSubmitDisplayStatus;
import com.example.cau_likelion_spring.assignment.domain.AssignmentSubmitStatus;
import com.example.cau_likelion_spring.assignment.domain.AssignmentType;
import com.example.cau_likelion_spring.assignment.domain.SubmissionFile;
import com.example.cau_likelion_spring.assignment.dto.AssignmentMemberSubmissionResponse;
import com.example.cau_likelion_spring.assignment.dto.AssignmentSubmitRequest;
import com.example.cau_likelion_spring.assignment.dto.AssignmentSubmitResponse;
import com.example.cau_likelion_spring.assignment.exception.AssignmentNotFoundException;
import com.example.cau_likelion_spring.assignment.exception.AssignmentPartMismatchException;
import com.example.cau_likelion_spring.assignment.exception.AssignmentSubmissionClosedException;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssignmentSubmitService {

    private static final int LATE_SUBMISSION_GRACE_DAYS = 5;

    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmitRepository assignmentSubmitRepository;
    private final SubmissionFileRepository submissionFileRepository;
    private final MemberRepository memberRepository;

    @PreAuthorize("hasRole('BABY_LION')")
    @Transactional
    public AssignmentSubmitResponse submit(Long memberId, Long assignmentId, AssignmentSubmitRequest request) {
        Assignment assignment = getAssignment(assignmentId);
        Member member = getMember(memberId);
        validateSamePart(assignment, member);
        validateContent(assignment, request);

        AssignmentSubmit latest = assignmentSubmitRepository
                .findFirstByAssignmentAndSubmitMemberOrderByCreatedAtDesc(assignment, member)
                .orElse(null);
        validateSubmittable(assignment, latest);

        AssignmentSubmit submit = assignmentSubmitRepository.save(AssignmentSubmit.builder()
                .assignment(assignment)
                .submitMember(member)
                .content(request.content())
                .url(request.url())
                .build());

        List<SubmissionFile> files = saveFiles(submit, request.files());

        return AssignmentSubmitResponse.of(submit, files, computeDisplayStatus(assignment, submit));
    }

    public AssignmentSubmitResponse getMySubmission(Long memberId, Long assignmentId) {
        Assignment assignment = getAssignment(assignmentId);
        Member member = getMember(memberId);
        validateSamePart(assignment, member);

        AssignmentSubmit latest = assignmentSubmitRepository
                .findFirstByAssignmentAndSubmitMemberOrderByCreatedAtDesc(assignment, member)
                .orElse(null);
        if (latest == null) {
            return null;
        }

        List<SubmissionFile> files = submissionFileRepository.findAllByAssignmentSubmit(latest);
        return AssignmentSubmitResponse.of(latest, files, computeDisplayStatus(assignment, latest));
    }

    /**
     * 아기사자 본인의 제출 이력 전체 (최신순). 재제출할 때마다 쌓인 이력을 다 보여준다.
     */
    @PreAuthorize("hasRole('BABY_LION')")
    public List<AssignmentSubmitResponse> getMySubmissionHistory(Long memberId, Long assignmentId) {
        Assignment assignment = getAssignment(assignmentId);
        Member member = getMember(memberId);
        validateSamePart(assignment, member);

        List<AssignmentSubmit> submits = assignmentSubmitRepository
                .findAllByAssignmentAndSubmitMemberOrderByCreatedAtDesc(assignment, member);
        if (submits.isEmpty()) {
            return List.of();
        }

        Map<Long, List<SubmissionFile>> filesBySubmitId = groupFilesBySubmitId(submits);

        return submits.stream()
                .map(submit -> AssignmentSubmitResponse.of(submit,
                        filesBySubmitId.getOrDefault(submit.getId(), List.of()),
                        computeDisplayStatus(assignment, submit)))
                .toList();
    }

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
                    AssignmentSubmitDisplayStatus displayStatus = computeDisplayStatus(assignment, latest);
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

    private void validateContent(Assignment assignment, AssignmentSubmitRequest request) {
        boolean hasUrl = StringUtils.hasText(request.url());
        boolean hasFiles = request.files() != null && !request.files().isEmpty();

        if (assignment.getType() == AssignmentType.URL) {
            if (!hasUrl) {
                throw new InvalidSubmissionException("URL 제출 형식인 과제입니다. url을 입력해주세요.");
            }
            if (hasFiles) {
                throw new InvalidSubmissionException("URL 제출 형식인 과제에는 파일을 첨부할 수 없습니다.");
            }
        } else {
            if (!hasFiles) {
                throw new InvalidSubmissionException("파일 제출 형식인 과제입니다. 파일을 1개 이상 첨부해주세요.");
            }
            if (hasUrl) {
                throw new InvalidSubmissionException("파일 제출 형식인 과제에는 URL을 입력할 수 없습니다.");
            }
        }
    }

    private void validateSubmittable(Assignment assignment, AssignmentSubmit latest) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endDate = assignment.getEndDate();

        if (latest == null) {
            if (now.isAfter(endDate.plusDays(LATE_SUBMISSION_GRACE_DAYS))) {
                throw new AssignmentSubmissionClosedException(assignment.getId());
            }
            return;
        }

        boolean afterDeadline = now.isAfter(endDate);
        boolean rejected = latest.getStatus() == AssignmentSubmitStatus.REJECTED;
        if (afterDeadline && !rejected) {
            throw new AssignmentSubmissionClosedException(assignment.getId());
        }
    }

    /**
     * 6가지 화면 표시 상태를 계산한다 (저장 없이 매번 계산).
     * 제출 이력 없음 -> 제출전/미제출, 있음 -> 상태(PENDING/APPROVED/REJECTED) + 지각 여부로 분기.
     */
    private AssignmentSubmitDisplayStatus computeDisplayStatus(Assignment assignment, AssignmentSubmit latest) {
        LocalDateTime endDate = assignment.getEndDate();

        if (latest == null) {
            boolean missed = LocalDateTime.now().isAfter(endDate.plusDays(LATE_SUBMISSION_GRACE_DAYS));
            return missed ? AssignmentSubmitDisplayStatus.MISSED : AssignmentSubmitDisplayStatus.BEFORE_SUBMISSION;
        }

        return switch (latest.getStatus()) {
            case APPROVED -> AssignmentSubmitDisplayStatus.APPROVED;
            case REJECTED -> AssignmentSubmitDisplayStatus.REJECTED;
            case PENDING -> latest.getCreatedAt().isAfter(endDate)
                    ? AssignmentSubmitDisplayStatus.LATE_SUBMITTED
                    : AssignmentSubmitDisplayStatus.PENDING_REVIEW;
        };
    }

    private List<SubmissionFile> saveFiles(AssignmentSubmit submit, List<AssignmentSubmitRequest.FileInfo> fileInfos) {
        if (fileInfos == null || fileInfos.isEmpty()) {
            return List.of();
        }

        List<SubmissionFile> files = fileInfos.stream()
                .map(fileInfo -> SubmissionFile.builder()
                        .assignmentSubmit(submit)
                        .fileUrl(fileInfo.fileUrl())
                        .originalFilename(fileInfo.originalFilename())
                        .build())
                .toList();

        return submissionFileRepository.saveAll(files);
    }

    private void validateSamePart(Assignment assignment, Member member) {
        Optional.ofNullable(member.getPart())
                .map(part -> part.getId().equals(assignment.getPart().getId()))
                .filter(Boolean::booleanValue)
                .orElseThrow(() -> new AssignmentPartMismatchException(assignment.getId()));
    }

    private Assignment getAssignment(Long id) {
        return assignmentRepository.findById(id)
                .orElseThrow(() -> new AssignmentNotFoundException(id));
    }

    private Member getMember(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new MemberNotFoundException(id));
    }

    private Part getStaffPart(Long staffMemberId) {
        Member staff = getMember(staffMemberId);
        Part part = staff.getPart();
        if (part == null) {
            throw new StaffPartNotAssignedException(staffMemberId);
        }
        return part;
    }
}

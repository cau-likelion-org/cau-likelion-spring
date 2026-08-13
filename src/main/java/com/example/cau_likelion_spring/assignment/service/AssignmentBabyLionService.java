package com.example.cau_likelion_spring.assignment.service;

import com.example.cau_likelion_spring.assignment.domain.Assignment;
import com.example.cau_likelion_spring.assignment.domain.AssignmentIndividualDeadline;
import com.example.cau_likelion_spring.assignment.domain.AssignmentSubmit;
import com.example.cau_likelion_spring.assignment.domain.AssignmentSubmitDisplayStatus;
import com.example.cau_likelion_spring.assignment.domain.AssignmentSubmitDisplayStatusCalculator;
import com.example.cau_likelion_spring.assignment.domain.AssignmentSubmitStatus;
import com.example.cau_likelion_spring.assignment.domain.AssignmentType;
import com.example.cau_likelion_spring.assignment.domain.SubmissionFile;
import com.example.cau_likelion_spring.assignment.dto.AssignmentSubmissionHistoryResponse;
import com.example.cau_likelion_spring.assignment.dto.AssignmentSubmitRequest;
import com.example.cau_likelion_spring.assignment.dto.AssignmentSubmitResponse;
import com.example.cau_likelion_spring.assignment.dto.AssignmentSummaryResponse;
import com.example.cau_likelion_spring.assignment.repository.AssignmentIndividualDeadlineRepository;
import com.example.cau_likelion_spring.assignment.repository.AssignmentRepository;
import com.example.cau_likelion_spring.assignment.repository.AssignmentSubmitRepository;
import com.example.cau_likelion_spring.assignment.repository.SubmissionFileRepository;
import com.example.cau_likelion_spring.member.domain.Member;
import com.example.cau_likelion_spring.global.exception.CustomException;
import com.example.cau_likelion_spring.global.exception.ErrorCode;
import com.example.cau_likelion_spring.global.util.S3Uploader;
import com.example.cau_likelion_spring.member.repository.MemberRepository;
import com.example.cau_likelion_spring.organization.domain.Part;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 아기사자(BABY_LION) 본인의 과제 목록 조회 및 과제 제출 관련 서비스.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssignmentBabyLionService {

    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmitRepository assignmentSubmitRepository;
    private final AssignmentIndividualDeadlineRepository assignmentIndividualDeadlineRepository;
    private final SubmissionFileRepository submissionFileRepository;
    private final MemberRepository memberRepository;
    private final S3Uploader s3Uploader;

    /**
     * week를 지정하면 해당 주차만, 지정하지 않으면 전체 주차를 주차 오름차순으로 묶어서 반환한다.
     * 같은 주차라도 과제마다 마감기한이 다를 수 있어 각 과제 생성 시점의 마감기한을 그대로 보여준다.
     */
    @PreAuthorize("hasRole('BABY_LION')")
    public List<AssignmentSummaryResponse.WeekGroup> getMyAssignments(Long memberId, Integer week) {
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

    /**
     * 최근 제출이 PENDING(운영진이 아직 평가하지 않음)이면 '수정'으로 보고 기존 row를 그대로 고치고,
     * 그 외(제출 이력 없음 / REJECTED)면 '제출' 또는 '재제출'로 보고 새 row를 만든다.
     * APPROVED(이미 승인됨)면 더 이상 제출할 수 없다.
     */
    @PreAuthorize("hasRole('BABY_LION')")
    @Transactional
    public AssignmentSubmitResponse submit(Long memberId, Long assignmentId, AssignmentSubmitRequest request) {
        Assignment assignment = getAssignment(assignmentId);
        Member member = getMember(memberId);
        validateSamePart(assignment, member);
        validateContent(assignment, request);

        LocalDateTime endDate = resolveEndDate(assignment, member);

        AssignmentSubmit latest = assignmentSubmitRepository
                .findFirstByAssignmentAndSubmitMemberOrderByCreatedAtDesc(assignment, member)
                .orElse(null);
        validateSubmittable(assignment, latest, endDate);

        boolean isEdit = latest != null && latest.getStatus() == AssignmentSubmitStatus.PENDING;
        AssignmentSubmit submit = isEdit
                ? editSubmission(latest, request)
                : createSubmission(assignment, member, request);

        List<SubmissionFile> files = saveFiles(submit, request.files());

        return AssignmentSubmitResponse.of(submit, files, AssignmentSubmitDisplayStatusCalculator.calculate(endDate, submit));
    }

    /**
     * week를 지정하면 해당 주차만, 지정하지 않으면 전체 주차를 주차 오름차순으로 묶어서 반환한다.
     * getMyAssignments와 달리 과제별 최신 상태 1건이 아니라, 아기사자 본인의 제출 이력 전체(최신순)를 과제마다 내려준다.
     */
    @PreAuthorize("hasRole('BABY_LION')")
    public List<AssignmentSubmissionHistoryResponse.WeekGroup> getMySubmissionHistoryByWeek(Long memberId, Integer week) {
        Member member = getMember(memberId);
        Part part = getMemberPart(member);

        List<Assignment> assignments = (week == null)
                ? assignmentRepository.findAllByPart_IdOrderByWeekAscEndDateAsc(part.getId())
                : assignmentRepository.findAllByPart_IdAndWeekOrderByEndDateAsc(part.getId(), week);
        if (assignments.isEmpty()) {
            return List.of();
        }

        List<Long> assignmentIds = assignments.stream().map(Assignment::getId).toList();
        List<AssignmentSubmit> allSubmits = assignmentSubmitRepository
                .findAllByAssignment_IdInAndSubmitMemberOrderByCreatedAtDesc(assignmentIds, member);

        Map<Long, List<AssignmentSubmit>> submitsByAssignmentId = allSubmits.stream()
                .collect(Collectors.groupingBy(submit -> submit.getAssignment().getId()));
        Map<Long, List<SubmissionFile>> filesBySubmitId = groupFilesBySubmitId(allSubmits);
        Map<Long, LocalDateTime> individualDeadlineByAssignmentId = findIndividualDeadlinesByAssignmentId(assignments, member);

        Map<Integer, List<Assignment>> assignmentsByWeek = assignments.stream()
                .collect(Collectors.groupingBy(Assignment::getWeek, LinkedHashMap::new, Collectors.toList()));

        return assignmentsByWeek.entrySet().stream()
                .map(entry -> toSubmissionHistoryWeekGroup(entry.getKey(), entry.getValue(), submitsByAssignmentId,
                        filesBySubmitId, individualDeadlineByAssignmentId))
                .toList();
    }

    /**
     * 아기사자 본인의 제출 이력 전체 (최신순). 수정/재제출할 때마다 쌓인 이력을 다 보여준다.
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

        LocalDateTime endDate = resolveEndDate(assignment, member);
        Map<Long, List<SubmissionFile>> filesBySubmitId = groupFilesBySubmitId(submits);

        return submits.stream()
                .map(submit -> AssignmentSubmitResponse.of(submit,
                        filesBySubmitId.getOrDefault(submit.getId(), List.of()),
                        AssignmentSubmitDisplayStatusCalculator.calculate(endDate, submit)))
                .toList();
    }

    private AssignmentSubmit editSubmission(AssignmentSubmit latest, AssignmentSubmitRequest request) {
        latest.editSubmission(request.content(), request.url());
        submissionFileRepository.findAllByAssignmentSubmit(latest)
                .forEach(file -> s3Uploader.deleteByUrl(file.getFileUrl()));
        submissionFileRepository.deleteAllByAssignmentSubmit(latest);
        return latest;
    }

    private AssignmentSubmit createSubmission(Assignment assignment, Member member, AssignmentSubmitRequest request) {
        return assignmentSubmitRepository.save(AssignmentSubmit.builder()
                .assignment(assignment)
                .submitMember(member)
                .content(request.content())
                .url(request.url())
                .build());
    }

    /** 개별 마감일이 있으면 그 값을, 없으면 과제 공통 마감일(Assignment.endDate)을 반환한다. */
    private LocalDateTime resolveEndDate(Assignment assignment, Member member) {
        return assignmentIndividualDeadlineRepository.findByAssignment_IdAndMember_Id(assignment.getId(), member.getId())
                .map(AssignmentIndividualDeadline::getDeadline)
                .orElse(assignment.getEndDate());
    }

    private void validateContent(Assignment assignment, AssignmentSubmitRequest request) {
        boolean hasUrl = StringUtils.hasText(request.url());
        boolean hasFiles = request.files() != null && !request.files().isEmpty();

        if (assignment.getType() == AssignmentType.URL) {
            if (!hasUrl) {
                throw new CustomException(ErrorCode.INVALID_INPUT, "URL 제출 형식인 과제입니다. url을 입력해주세요.");
            }
            if (hasFiles) {
                throw new CustomException(ErrorCode.INVALID_INPUT, "URL 제출 형식인 과제에는 파일을 첨부할 수 없습니다.");
            }
        } else {
            if (!hasFiles) {
                throw new CustomException(ErrorCode.INVALID_INPUT, "파일 제출 형식인 과제입니다. 파일을 1개 이상 첨부해주세요.");
            }
            if (hasUrl) {
                throw new CustomException(ErrorCode.INVALID_INPUT, "파일 제출 형식인 과제에는 URL을 입력할 수 없습니다.");
            }
        }
    }

    private void validateSubmittable(Assignment assignment, AssignmentSubmit latest, LocalDateTime endDate) {
        LocalDateTime now = LocalDateTime.now();

        if (latest == null) {
            if (now.isAfter(endDate.plusDays(AssignmentSubmitDisplayStatusCalculator.LATE_SUBMISSION_GRACE_DAYS))) {
                throw new CustomException(ErrorCode.ASSIGNMENT_DEADLINE_PASSED, "제출 가능 기한이 지나 더 이상 제출할 수 없습니다. assignmentId=" + assignment.getId());
            }
            return;
        }

        if (latest.getStatus() == AssignmentSubmitStatus.APPROVED) {
            throw new CustomException(ErrorCode.ASSIGNMENT_ALREADY_SUBMITTED, "이미 승인된 과제는 다시 제출할 수 없습니다. assignmentId=" + assignment.getId());
        }

        boolean afterDeadline = now.isAfter(endDate);
        boolean rejected = latest.getStatus() == AssignmentSubmitStatus.REJECTED;
        if (afterDeadline && !rejected) {
            throw new CustomException(ErrorCode.ASSIGNMENT_DEADLINE_PASSED, "제출 가능 기한이 지나 더 이상 제출할 수 없습니다. assignmentId=" + assignment.getId());
        }
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
                .orElseThrow(() -> new CustomException(ErrorCode.ASSIGNMENT_PART_MISMATCH, "본인 파트의 과제만 관리할 수 있습니다. assignmentId=" + assignment.getId()));
    }

    private Map<Long, List<SubmissionFile>> groupFilesBySubmitId(List<AssignmentSubmit> submits) {
        return submissionFileRepository.findAllByAssignmentSubmitIn(submits).stream()
                .collect(Collectors.groupingBy(file -> file.getAssignmentSubmit().getId()));
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

    private AssignmentSummaryResponse.WeekGroup toWeekGroup(Integer week, List<Assignment> weekAssignments,
                                                              Map<Long, AssignmentSubmit> latestSubmitByAssignmentId,
                                                              Map<Long, LocalDateTime> individualDeadlineByAssignmentId) {
        List<AssignmentSummaryResponse> assignmentSummaries = weekAssignments.stream()
                .map(assignment -> toSummary(assignment, latestSubmitByAssignmentId.get(assignment.getId()),
                        individualDeadlineByAssignmentId.getOrDefault(assignment.getId(), assignment.getEndDate())))
                .toList();

        AssignmentSubmitDisplayStatus weeklyStatus = AssignmentSubmitDisplayStatusCalculator.aggregateWeekly(
                assignmentSummaries.stream().map(AssignmentSummaryResponse::status).toList());

        return new AssignmentSummaryResponse.WeekGroup(week, weeklyStatus, assignmentSummaries);
    }

    private AssignmentSummaryResponse toSummary(Assignment assignment, AssignmentSubmit latest, LocalDateTime endDate) {
        AssignmentSubmitDisplayStatus status = AssignmentSubmitDisplayStatusCalculator.calculate(endDate, latest);
        LocalDateTime submittedAt = latest == null ? null : latest.getUpdatedAt();
        return new AssignmentSummaryResponse(assignment.getId(), assignment.getTitle(), endDate, status, submittedAt);
    }

    private AssignmentSubmissionHistoryResponse.WeekGroup toSubmissionHistoryWeekGroup(
            Integer week, List<Assignment> weekAssignments, Map<Long, List<AssignmentSubmit>> submitsByAssignmentId,
            Map<Long, List<SubmissionFile>> filesBySubmitId, Map<Long, LocalDateTime> individualDeadlineByAssignmentId) {
        List<AssignmentSubmissionHistoryResponse> assignmentHistories = weekAssignments.stream()
                .map(assignment -> toSubmissionHistory(assignment,
                        submitsByAssignmentId.getOrDefault(assignment.getId(), List.of()), filesBySubmitId,
                        individualDeadlineByAssignmentId.getOrDefault(assignment.getId(), assignment.getEndDate())))
                .toList();

        AssignmentSubmitDisplayStatus weeklyStatus = AssignmentSubmitDisplayStatusCalculator.aggregateWeekly(
                weekAssignments.stream()
                        .map(assignment -> currentStatus(
                                submitsByAssignmentId.getOrDefault(assignment.getId(), List.of()),
                                individualDeadlineByAssignmentId.getOrDefault(assignment.getId(), assignment.getEndDate())))
                        .toList());

        return new AssignmentSubmissionHistoryResponse.WeekGroup(week, weeklyStatus, assignmentHistories);
    }

    private AssignmentSubmissionHistoryResponse toSubmissionHistory(Assignment assignment, List<AssignmentSubmit> submits,
                                                                      Map<Long, List<SubmissionFile>> filesBySubmitId,
                                                                      LocalDateTime endDate) {
        List<AssignmentSubmitResponse> history = submits.stream()
                .map(submit -> AssignmentSubmitResponse.of(submit, filesBySubmitId.getOrDefault(submit.getId(), List.of()),
                        AssignmentSubmitDisplayStatusCalculator.calculate(endDate, submit)))
                .toList();
        return new AssignmentSubmissionHistoryResponse(assignment.getId(), assignment.getTitle(), assignment.getDetail(),
                endDate, history);
    }

    private AssignmentSubmitDisplayStatus currentStatus(List<AssignmentSubmit> submits, LocalDateTime endDate) {
        AssignmentSubmit latest = submits.isEmpty() ? null : submits.get(0);
        return AssignmentSubmitDisplayStatusCalculator.calculate(endDate, latest);
    }

    private Assignment getAssignment(Long id) {
        return assignmentRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.ASSIGNMENT_NOT_FOUND, "존재하지 않는 과제입니다. id=" + id));
    }

    private Member getMember(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND, "존재하지 않는 구성원입니다. id=" + id));
    }

    private Part getMemberPart(Member member) {
        Part part = member.getPart();
        if (part == null) {
            throw new CustomException(ErrorCode.ASSIGNMENT_BABY_LION_PART_NOT_ASSIGNED, "아기사자에게 배정된 파트가 없습니다. memberId=" + member.getId());
        }
        return part;
    }
}

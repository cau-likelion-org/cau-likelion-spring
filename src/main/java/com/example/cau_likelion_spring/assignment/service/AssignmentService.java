package com.example.cau_likelion_spring.assignment.service;

import com.example.cau_likelion_spring.assignment.domain.Assignment;
import com.example.cau_likelion_spring.assignment.domain.AssignmentIndividualDeadline;
import com.example.cau_likelion_spring.assignment.domain.AssignmentSubmit;
import com.example.cau_likelion_spring.assignment.domain.AssignmentSubmitDisplayStatus;
import com.example.cau_likelion_spring.assignment.domain.AssignmentSubmitDisplayStatusCalculator;
import com.example.cau_likelion_spring.assignment.domain.SubmissionFile;
import com.example.cau_likelion_spring.assignment.dto.AssignmentCreateRequest;
import com.example.cau_likelion_spring.assignment.dto.AssignmentIndividualDeadlineRequest;
import com.example.cau_likelion_spring.assignment.dto.AssignmentIndividualDeadlineResponse;
import com.example.cau_likelion_spring.assignment.dto.AssignmentMemberSubmissionHistoryResponse;
import com.example.cau_likelion_spring.assignment.dto.AssignmentMemberSubmissionResponse;
import com.example.cau_likelion_spring.assignment.dto.AssignmentMemberWeeklyStatusResponse;
import com.example.cau_likelion_spring.assignment.dto.AssignmentResponse;
import com.example.cau_likelion_spring.assignment.dto.AssignmentStaffDetailResponse;
import com.example.cau_likelion_spring.assignment.dto.AssignmentStaffSubmissionHistoryResponse;
import com.example.cau_likelion_spring.assignment.dto.AssignmentStaffSummaryResponse;
import com.example.cau_likelion_spring.assignment.dto.AssignmentSubmitEvaluateRequest;
import com.example.cau_likelion_spring.assignment.dto.AssignmentSubmitResponse;
import com.example.cau_likelion_spring.assignment.dto.AssignmentUpdateRequest;
import com.example.cau_likelion_spring.assignment.repository.AssignmentIndividualDeadlineRepository;
import com.example.cau_likelion_spring.assignment.repository.AssignmentRepository;
import com.example.cau_likelion_spring.assignment.repository.AssignmentSubmitRepository;
import com.example.cau_likelion_spring.assignment.repository.PushNotiLogRepository;
import com.example.cau_likelion_spring.assignment.repository.SubmissionFileRepository;
import com.example.cau_likelion_spring.member.domain.Member;
import com.example.cau_likelion_spring.member.domain.MemberRole;
import com.example.cau_likelion_spring.global.exception.CustomException;
import com.example.cau_likelion_spring.global.exception.ErrorCode;
import com.example.cau_likelion_spring.global.util.S3Uploader;
import com.example.cau_likelion_spring.member.repository.MemberRepository;
import com.example.cau_likelion_spring.organization.domain.Part;
import com.example.cau_likelion_spring.organization.repository.PartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 운영진(STAFF)/회장(PRESIDENT)의 과제 생성/수정/삭제/개별 마감일 관리 및
 * 파트원 과제 제출 현황 조회·평가 서비스.
 */
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
    private final AssignmentPushNotificationService assignmentPushNotificationService;
    private final S3Uploader s3Uploader;

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
    public AssignmentResponse getById(Long staffMemberId, Long assignmentId) {
        return AssignmentResponse.of(getOwnedAssignment(staffMemberId, assignmentId));
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

        submissionFileRepository.findAllByAssignmentSubmit_Assignment(assignment)
                .forEach(file -> s3Uploader.deleteByUrl(file.getFileUrl()));
        pushNotiLogRepository.deleteAllByAssignmentSubmit_Assignment(assignment);
        submissionFileRepository.deleteAllByAssignmentSubmit_Assignment(assignment);
        assignmentSubmitRepository.deleteAllByAssignment(assignment);
        assignmentIndividualDeadlineRepository.deleteAllByAssignment(assignment);
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
            throw new CustomException(ErrorCode.MEMBER_NOT_FOUND, "존재하지 않는 구성원입니다. id=" + missingId);
        }
        for (Member member : members) {
            if (member.getPart() == null || !member.getPart().getId().equals(assignment.getPart().getId())) {
                throw new CustomException(ErrorCode.ASSIGNMENT_MEMBER_PART_MISMATCH,
                        "과제 파트에 속하지 않은 아기사자입니다. memberId=" + member.getId());
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
    public List<AssignmentStaffSummaryResponse.WeekGroup> getMyAssignmentsForStaff(Long staffMemberId) {
        Part part = getStaffPart(staffMemberId);
        return getAssignmentsByPart(part);
    }

    /**
     * 회장이 보는 특정 파트 과제 목록(주차별). 파트를 파라미터로 직접 지정한다는 점을 제외하면
     * {@link #getMyAssignmentsForStaff}와 동일한 집계 로직을 사용한다.
     */
    @PreAuthorize("hasAnyRole('PRESIDENT', 'ADMIN')")
    public List<AssignmentStaffSummaryResponse.WeekGroup> getAssignmentsForPresident(Long partId) {
        Part part = getPart(partId);
        return getAssignmentsByPart(part);
    }

    /**
     * 운영진이 보는 본인 파트 아기사자 1명의 특정 주차 종합 상태. 그 주차에 개별 과제가 여러 개면 상태들을
     * 우선순위(제출전 > 승인반려 > 승인대기 > 미제출 > 지각제출 > 승인완료)로 판단해 상태 하나로 합친다.
     */
    @PreAuthorize("hasRole('STAFF')")
    public AssignmentMemberWeeklyStatusResponse getWeeklyStatusForStaff(Long staffMemberId, Long targetMemberId, Integer week) {
        Part staffPart = getStaffPart(staffMemberId);
        Member babyLion = getMember(targetMemberId);
        if (babyLion.getPart() == null || !babyLion.getPart().getId().equals(staffPart.getId())) {
            throw new CustomException(ErrorCode.ASSIGNMENT_MEMBER_PART_MISMATCH,
                    "과제 파트에 속하지 않은 아기사자입니다. memberId=" + targetMemberId);
        }

        List<Assignment> assignments = assignmentRepository.findAllByPart_IdAndWeekOrderByEndDateAsc(staffPart.getId(), week);
        if (assignments.isEmpty()) {
            throw new CustomException(ErrorCode.ASSIGNMENT_NOT_FOUND, "해당 주차에 생성된 과제가 없습니다. week=" + week);
        }

        StatusLookup statusLookup = buildStatusLookup(assignments);
        List<AssignmentSubmitDisplayStatus> statuses = assignments.stream()
                .map(assignment -> statusLookup.statusOf(assignment, babyLion.getId()))
                .toList();

        AssignmentSubmitDisplayStatus weeklyStatus = AssignmentSubmitDisplayStatusCalculator.aggregateWeekly(statuses);
        return new AssignmentMemberWeeklyStatusResponse(babyLion.getId(), babyLion.getName(), week, weeklyStatus);
    }

    /**
     * 운영진이 보는 파트원 전체의 제출 이력. 파트원별로 제출 이력을 전부(최신순) 보여준다 -
     * 반려 후 재제출처럼 같은 파트원이 같은 과제에 여러 번 제출했다면 그 이력이 모두 노출된다.
     * 한 번도 제출하지 않은 파트원도 "제출전"/"미제출" 상태로 빈 이력과 함께 포함된다.
     * 과제 자체의 제목/설명/마감기한도 함께 내려준다.
     */
    @PreAuthorize("hasRole('STAFF')")
    public AssignmentStaffSubmissionHistoryResponse getSubmissionsForStaff(Long staffMemberId, Long assignmentId) {
        Assignment assignment = getAssignment(assignmentId);
        Part staffPart = getStaffPart(staffMemberId);
        if (!assignment.getPart().getId().equals(staffPart.getId())) {
            throw new CustomException(ErrorCode.ASSIGNMENT_PART_MISMATCH, "본인 파트의 과제만 관리할 수 있습니다. assignmentId=" + assignmentId);
        }

        List<Member> babyLions = memberRepository.findByPart_IdAndRole(assignment.getPart().getId(), MemberRole.BABY_LION);
        List<AssignmentMemberSubmissionHistoryResponse> submissions = babyLions.isEmpty()
                ? List.of()
                : buildMemberSubmissionHistories(assignment, babyLions);

        return new AssignmentStaffSubmissionHistoryResponse(assignment.getId(), assignment.getTitle(), assignment.getDetail(),
                assignment.getEndDate(), submissions);
    }

    private List<AssignmentMemberSubmissionHistoryResponse> buildMemberSubmissionHistories(Assignment assignment,
                                                                                             List<Member> babyLions) {
        List<Long> memberIds = babyLions.stream().map(Member::getId).toList();
        List<AssignmentSubmit> allSubmits = assignmentSubmitRepository
                .findAllByAssignmentAndSubmitMember_IdInOrderByCreatedAtDesc(assignment, memberIds);

        Map<Long, List<AssignmentSubmit>> submitsByMemberId = allSubmits.stream()
                .collect(Collectors.groupingBy(submit -> submit.getSubmitMember().getId()));
        Map<Long, List<SubmissionFile>> filesBySubmitId = groupFilesBySubmitId(allSubmits);
        Map<Long, LocalDateTime> individualDeadlineByMemberId = assignmentIndividualDeadlineRepository
                .findAllByAssignment_IdAndMember_IdIn(assignment.getId(), memberIds).stream()
                .collect(Collectors.toMap(deadline -> deadline.getMember().getId(), AssignmentIndividualDeadline::getDeadline));

        return babyLions.stream()
                .map(member -> {
                    List<AssignmentSubmit> submits = submitsByMemberId.getOrDefault(member.getId(), List.of());
                    LocalDateTime endDate = individualDeadlineByMemberId.getOrDefault(member.getId(), assignment.getEndDate());
                    return toMemberSubmissionHistory(member, endDate, submits, filesBySubmitId);
                })
                .toList();
    }

    /**
     * 운영진이 보는 본인 파트 과제별 파트원 전체 제출 현황(주차별로 묶임). 화면에서 주차 → 개별 과제 → 아기사자별
     * 내역(이름/최종 제출 시각/제출물/상태/리뷰 운영진 이름) 순으로 펼쳐 보여주고, 평가 버튼에 필요한 submitId도 함께 내려준다.
     */
    @PreAuthorize("hasRole('STAFF')")
    public List<AssignmentStaffDetailResponse.WeekGroup> getSubmissionStatusForStaff(Long staffMemberId) {
        Part part = getStaffPart(staffMemberId);

        List<Assignment> assignments = assignmentRepository.findAllByPart_IdOrderByWeekAscEndDateAsc(part.getId());
        if (assignments.isEmpty()) {
            return List.of();
        }

        List<Member> babyLions = memberRepository.findByPart_IdAndRole(part.getId(), MemberRole.BABY_LION);

        StatusLookup statusLookup = buildStatusLookup(assignments);
        Map<Long, List<SubmissionFile>> filesBySubmitId = groupFilesBySubmitId(
                statusLookup.latestByAssignmentIdThenMemberId().values().stream()
                        .flatMap(byMemberId -> byMemberId.values().stream())
                        .toList());

        Map<Integer, List<Assignment>> assignmentsByWeek = assignments.stream()
                .collect(Collectors.groupingBy(Assignment::getWeek, LinkedHashMap::new, Collectors.toList()));

        return assignmentsByWeek.entrySet().stream()
                .map(entry -> toStaffDetailWeekGroup(entry.getKey(), entry.getValue(), babyLions, statusLookup, filesBySubmitId))
                .toList();
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
            throw new CustomException(ErrorCode.ASSIGNMENT_PART_MISMATCH, "본인 파트의 과제만 관리할 수 있습니다. assignmentId=" + assignmentId);
        }

        AssignmentSubmit submit = getSubmit(submitId);
        if (!submit.getAssignment().getId().equals(assignmentId)) {
            throw new CustomException(ErrorCode.ASSIGNMENT_SUBMIT_NOT_FOUND, "존재하지 않는 제출입니다. submitId=" + submitId);
        }

        switch (request.status()) {
            case APPROVED -> submit.approve(staff);
            case REJECTED -> {
                if (!StringUtils.hasText(request.rejectionReason())) {
                    throw new CustomException(ErrorCode.INVALID_INPUT, "반려 사유를 입력해주세요.");
                }
                submit.reject(staff, request.rejectionReason());
            }
            case PENDING -> throw new CustomException(ErrorCode.INVALID_INPUT, "평가 상태는 APPROVED 또는 REJECTED만 가능합니다.");
        }

        assignmentPushNotificationService.sendEvaluationNotification(submit);

        LocalDateTime endDate = assignmentIndividualDeadlineRepository
                .findByAssignment_IdAndMember_Id(assignmentId, submit.getSubmitMember().getId())
                .map(AssignmentIndividualDeadline::getDeadline)
                .orElse(assignment.getEndDate());

        List<SubmissionFile> files = submissionFileRepository.findAllByAssignmentSubmit(submit);
        AssignmentSubmitDisplayStatus displayStatus = AssignmentSubmitDisplayStatusCalculator.calculate(endDate, submit);
        return AssignmentSubmitResponse.of(submit, files, displayStatus);
    }

    private List<AssignmentStaffSummaryResponse.WeekGroup> getAssignmentsByPart(Part part) {
        List<Assignment> assignments = assignmentRepository.findAllByPart_IdOrderByWeekAscEndDateAsc(part.getId());
        if (assignments.isEmpty()) {
            return List.of();
        }

        List<Member> babyLions = memberRepository.findByPart_IdAndRole(part.getId(), MemberRole.BABY_LION);
        StatusLookup statusLookup = buildStatusLookup(assignments);

        Map<Integer, List<Assignment>> assignmentsByWeek = assignments.stream()
                .collect(Collectors.groupingBy(Assignment::getWeek, LinkedHashMap::new, Collectors.toList()));

        return assignmentsByWeek.entrySet().stream()
                .map(entry -> toStaffWeekGroup(entry.getKey(), entry.getValue(), babyLions, statusLookup))
                .toList();
    }

    /**
     * 과제×멤버별 최신 제출과 유효 마감일(개별 마감일 우선, 없으면 과제 공통 마감일)을 한 번에 조회해서 묶는다.
     * staff 요약/상세/주차별 종합 상태 조회가 모두 이 헬퍼를 공유해서 상태를 계산한다.
     */
    private StatusLookup buildStatusLookup(List<Assignment> assignments) {
        List<Long> assignmentIds = assignments.stream().map(Assignment::getId).toList();

        Map<Long, Map<Long, AssignmentSubmit>> latestByAssignmentIdThenMemberId =
                assignmentSubmitRepository.findAllByAssignment_IdInOrderByCreatedAtDesc(assignmentIds).stream()
                        .collect(Collectors.groupingBy(submit -> submit.getAssignment().getId(),
                                Collectors.toMap(submit -> submit.getSubmitMember().getId(), submit -> submit,
                                        (firstByCreatedAtDesc, ignoredOlder) -> firstByCreatedAtDesc)));

        Map<Long, Map<Long, LocalDateTime>> deadlineByAssignmentIdThenMemberId =
                assignmentIndividualDeadlineRepository.findAllByAssignment_IdIn(assignmentIds).stream()
                        .collect(Collectors.groupingBy(deadline -> deadline.getAssignment().getId(),
                                Collectors.toMap(deadline -> deadline.getMember().getId(), AssignmentIndividualDeadline::getDeadline)));

        return new StatusLookup(latestByAssignmentIdThenMemberId, deadlineByAssignmentIdThenMemberId);
    }

    private AssignmentStaffSummaryResponse.WeekGroup toStaffWeekGroup(Integer week, List<Assignment> weekAssignments,
                                                                       List<Member> babyLions, StatusLookup statusLookup) {
        List<AssignmentStaffSummaryResponse> assignmentSummaries = weekAssignments.stream()
                .map(assignment -> toStaffSummary(assignment, babyLions, statusLookup))
                .toList();

        return new AssignmentStaffSummaryResponse.WeekGroup(week, assignmentSummaries);
    }

    private AssignmentStaffSummaryResponse toStaffSummary(Assignment assignment, List<Member> babyLions, StatusLookup statusLookup) {
        int beforeSubmissionCount = 0;
        int missedCount = 0;
        int pendingReviewCount = 0;
        int lateSubmittedCount = 0;
        int approvedCount = 0;

        for (Member babyLion : babyLions) {
            AssignmentSubmitDisplayStatus status = statusLookup.statusOf(assignment, babyLion.getId());
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

    private AssignmentStaffDetailResponse.WeekGroup toStaffDetailWeekGroup(Integer week, List<Assignment> weekAssignments,
                                                                            List<Member> babyLions, StatusLookup statusLookup,
                                                                            Map<Long, List<SubmissionFile>> filesBySubmitId) {
        List<AssignmentStaffDetailResponse> assignmentDetails = weekAssignments.stream()
                .map(assignment -> toStaffDetail(assignment, babyLions, statusLookup, filesBySubmitId))
                .toList();

        return new AssignmentStaffDetailResponse.WeekGroup(week, assignmentDetails);
    }

    private AssignmentStaffDetailResponse toStaffDetail(Assignment assignment, List<Member> babyLions,
                                                          StatusLookup statusLookup, Map<Long, List<SubmissionFile>> filesBySubmitId) {
        List<AssignmentMemberSubmissionResponse> submissions = babyLions.stream()
                .map(member -> {
                    AssignmentSubmit latest = statusLookup.latestSubmitOf(assignment, member.getId());
                    LocalDateTime endDate = statusLookup.effectiveDeadlineOf(assignment, member.getId());
                    return toMemberSubmission(member, endDate, latest, filesBySubmitId);
                })
                .toList();

        return new AssignmentStaffDetailResponse(assignment.getId(), assignment.getTitle(), assignment.getDetail(),
                assignment.getEndDate(), submissions);
    }

    private AssignmentMemberSubmissionResponse toMemberSubmission(Member member, LocalDateTime endDate, AssignmentSubmit latest,
                                                                    Map<Long, List<SubmissionFile>> filesBySubmitId) {
        AssignmentSubmitDisplayStatus displayStatus = AssignmentSubmitDisplayStatusCalculator.calculate(endDate, latest);
        AssignmentSubmitResponse latestResponse = latest == null ? null
                : AssignmentSubmitResponse.of(latest, filesBySubmitId.getOrDefault(latest.getId(), List.of()), displayStatus);
        return AssignmentMemberSubmissionResponse.of(member, displayStatus, latestResponse);
    }

    private AssignmentMemberSubmissionHistoryResponse toMemberSubmissionHistory(Member member, LocalDateTime endDate,
                                                                                  List<AssignmentSubmit> submits,
                                                                                  Map<Long, List<SubmissionFile>> filesBySubmitId) {
        AssignmentSubmit latest = submits.isEmpty() ? null : submits.get(0);
        AssignmentSubmitDisplayStatus displayStatus = AssignmentSubmitDisplayStatusCalculator.calculate(endDate, latest);
        List<AssignmentSubmitResponse> history = submits.stream()
                .map(submit -> AssignmentSubmitResponse.of(submit, filesBySubmitId.getOrDefault(submit.getId(), List.of()),
                        AssignmentSubmitDisplayStatusCalculator.calculate(endDate, submit)))
                .toList();
        return AssignmentMemberSubmissionHistoryResponse.of(member, endDate, displayStatus, history);
    }

    private Map<Long, List<SubmissionFile>> groupFilesBySubmitId(List<AssignmentSubmit> submits) {
        return submissionFileRepository.findAllByAssignmentSubmitIn(submits).stream()
                .collect(Collectors.groupingBy(file -> file.getAssignmentSubmit().getId()));
    }

    private AssignmentSubmit getSubmit(Long submitId) {
        return assignmentSubmitRepository.findById(submitId)
                .orElseThrow(() -> new CustomException(ErrorCode.ASSIGNMENT_SUBMIT_NOT_FOUND, "존재하지 않는 제출입니다. submitId=" + submitId));
    }

    private Assignment getOwnedAssignment(Long staffMemberId, Long assignmentId) {
        Part staffPart = getStaffPart(staffMemberId);
        Assignment assignment = getAssignment(assignmentId);

        if (!assignment.getPart().getId().equals(staffPart.getId())) {
            throw new CustomException(ErrorCode.ASSIGNMENT_PART_MISMATCH, "본인 파트의 과제만 관리할 수 있습니다. assignmentId=" + assignmentId);
        }

        return assignment;
    }

    private Assignment getAssignment(Long id) {
        return assignmentRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.ASSIGNMENT_NOT_FOUND, "존재하지 않는 과제입니다. id=" + id));
    }

    private Part getPart(Long partId) {
        return partRepository.findById(partId)
                .orElseThrow(() -> new CustomException(ErrorCode.PART_NOT_FOUND, "존재하지 않는 파트입니다. id=" + partId));
    }

    private Part getStaffPart(Long staffMemberId) {
        Member staff = getMember(staffMemberId);

        Part part = staff.getPart();
        if (part == null) {
            throw new CustomException(ErrorCode.ASSIGNMENT_STAFF_PART_NOT_ASSIGNED, "운영진에게 배정된 파트가 없습니다. memberId=" + staffMemberId);
        }

        return part;
    }

    private Member getMember(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND, "존재하지 않는 구성원입니다. id=" + id));
    }

    /**
     * 과제×멤버별 최신 제출/유효 마감일 조회 결과를 묶어, 호출부는 assignment와 memberId만으로
     * 화면 표시 상태를 바로 얻을 수 있게 해준다. {@link #buildStatusLookup}으로 생성한다.
     */
    private record StatusLookup(
            Map<Long, Map<Long, AssignmentSubmit>> latestByAssignmentIdThenMemberId,
            Map<Long, Map<Long, LocalDateTime>> deadlineByAssignmentIdThenMemberId
    ) {
        AssignmentSubmit latestSubmitOf(Assignment assignment, Long memberId) {
            return latestByAssignmentIdThenMemberId.getOrDefault(assignment.getId(), Map.of()).get(memberId);
        }

        LocalDateTime effectiveDeadlineOf(Assignment assignment, Long memberId) {
            return deadlineByAssignmentIdThenMemberId.getOrDefault(assignment.getId(), Map.of())
                    .getOrDefault(memberId, assignment.getEndDate());
        }

        AssignmentSubmitDisplayStatus statusOf(Assignment assignment, Long memberId) {
            return AssignmentSubmitDisplayStatusCalculator.calculate(
                    effectiveDeadlineOf(assignment, memberId), latestSubmitOf(assignment, memberId));
        }
    }
}

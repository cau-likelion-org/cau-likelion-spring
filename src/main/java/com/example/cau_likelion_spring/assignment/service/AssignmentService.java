package com.example.cau_likelion_spring.assignment.service;

import com.example.cau_likelion_spring.assignment.domain.Assignment;
import com.example.cau_likelion_spring.assignment.domain.AssignmentIndividualDeadline;
import com.example.cau_likelion_spring.assignment.domain.AssignmentSubmit;
import com.example.cau_likelion_spring.assignment.domain.AssignmentSubmitDisplayStatus;
import com.example.cau_likelion_spring.assignment.domain.AssignmentSubmitDisplayStatusCalculator;
import com.example.cau_likelion_spring.assignment.domain.AssignmentSubmitStatus;
import com.example.cau_likelion_spring.assignment.domain.SubmissionFile;
import com.example.cau_likelion_spring.assignment.dto.AssignmentCreateRequest;
import com.example.cau_likelion_spring.assignment.dto.AssignmentIndividualDeadlineRequest;
import com.example.cau_likelion_spring.assignment.dto.AssignmentIndividualDeadlineResponse;
import com.example.cau_likelion_spring.assignment.dto.AssignmentMemberSubmissionHistoryResponse;
import com.example.cau_likelion_spring.assignment.dto.AssignmentMemberSubmissionResponse;
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
import jakarta.persistence.EntityManager;
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
 * 과제 생성/수정/삭제/개별 마감일 관리와 제출 평가({@link #evaluate})는 STAFF/ADMIN/PRESIDENT 모두
 * 본인 소속 파트로 제한된다. 반면 조회 계열({@link #getById}, {@link #getSubmissionsForStaff},
 * {@link #getMyAssignmentsForStaff}, {@link #getSubmissionStatusForStaff})은
 * STAFF는 본인 파트로 제한되고 ADMIN/PRESIDENT는 전체 파트를 조회할 수 있다.
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
    private final EntityManager entityManager;

    /**
     * 한 주차에 개별 과제 1개 이상을 한 번에 생성한다 (생성 페이지에서 +로 여러 개를 모아 한 번에 저장하는 흐름).
     * ADMIN도 STAFF와 동일하게 본인 소속 파트에 한해 생성할 수 있다.
     */
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN', 'PRESIDENT')")
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

    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN', 'PRESIDENT')")
    public AssignmentResponse getById(Long staffMemberId, Long assignmentId) {
        return AssignmentResponse.of(getViewableAssignment(staffMemberId, assignmentId));
    }

    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN', 'PRESIDENT')")
    @Transactional
    public AssignmentResponse update(Long staffMemberId, Long assignmentId, AssignmentUpdateRequest request) {
        Assignment assignment = getManageableAssignment(staffMemberId, assignmentId);

        assignment.update(request.title(), request.detail(), request.endDate(), request.type());

        return AssignmentResponse.of(assignment);
    }

    /**
     * 과제 삭제. 이전에 제출된 과제(AssignmentSubmit)와 그에 딸린 파일/알림 이력도 함께 삭제된다.
     * (수정과 달리 삭제는 제출 이력을 보존하지 않음)
     */
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN', 'PRESIDENT')")
    @Transactional
    public void delete(Long staffMemberId, Long assignmentId) {
        Assignment assignment = getManageableAssignment(staffMemberId, assignmentId);

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
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN', 'PRESIDENT')")
    @Transactional
    public List<AssignmentIndividualDeadlineResponse> updateIndividualDeadlines(Long staffMemberId, Long assignmentId,
                                                                                  AssignmentIndividualDeadlineRequest request) {
        Assignment assignment = getManageableAssignment(staffMemberId, assignmentId);

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
     * 운영진이 보는 과제 목록(주차별). STAFF는 본인 파트, ADMIN/PRESIDENT는 전체 파트 대상이다.
     * 과제마다 파트원 전체를 대상으로 최신 제출 기준 제출전/미제출/승인대기/지각제출/승인완료 인원 수를 함께 집계해서 보여준다.
     */
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN', 'PRESIDENT')")
    public List<AssignmentStaffSummaryResponse.WeekGroup> getMyAssignmentsForStaff(Long staffMemberId) {
        List<Assignment> assignments = isAdminOrPresident(staffMemberId)
                ? assignmentRepository.findAllByOrderByWeekAscEndDateAsc()
                : assignmentRepository.findAllByPart_IdOrderByWeekAscEndDateAsc(getStaffPart(staffMemberId).getId());
        return toStaffSummaryWeekGroups(assignments);
    }

    /**
     * 회장/관리자가 보는 특정 파트 과제 목록(주차별). 파트를 파라미터로 직접 지정한다는 점을 제외하면
     * {@link #getMyAssignmentsForStaff}와 동일한 집계 로직을 사용한다.
     */
    @PreAuthorize("hasAnyRole('PRESIDENT', 'ADMIN')")
    public List<AssignmentStaffSummaryResponse.WeekGroup> getAssignmentsForPresident(Long partId) {
        Part part = getPart(partId);
        List<Assignment> assignments = assignmentRepository.findAllByPart_IdOrderByWeekAscEndDateAsc(part.getId());
        return toStaffSummaryWeekGroups(assignments);
    }

    /**
     * 파트원 전체의 제출 이력. STAFF는 본인 파트 과제만, ADMIN/PRESIDENT는 파트 제한 없이 조회 가능하다.
     * 파트원별로 제출 이력을 전부(최신순) 보여준다 - 반려 후 재제출처럼 같은 파트원이 같은 과제에 여러 번 제출했다면
     * 그 이력이 모두 노출된다. 한 번도 제출하지 않은 파트원도 "제출전"/"미제출" 상태로 빈 이력과 함께 포함된다.
     * 과제 자체의 제목/설명/마감기한도 함께 내려준다.
     */
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN', 'PRESIDENT')")
    public AssignmentStaffSubmissionHistoryResponse getSubmissionsForStaff(Long staffMemberId, Long assignmentId) {
        Assignment assignment = getViewableAssignment(staffMemberId, assignmentId);

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
     * 과제별 파트원 전체 제출 현황(주차별로 묶임). STAFF는 본인 파트, ADMIN/PRESIDENT는 전체 파트 대상이다.
     * 화면에서 주차 → 개별 과제 → 아기사자별 내역(이름/최종 제출 시각/제출물/상태/리뷰 운영진 이름) 순으로 펼쳐 보여주고,
     * 평가 버튼에 필요한 submitId도 함께 내려준다.
     */
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN', 'PRESIDENT')")
    public List<AssignmentStaffDetailResponse.WeekGroup> getSubmissionStatusForStaff(Long staffMemberId) {
        List<Assignment> assignments = isAdminOrPresident(staffMemberId)
                ? assignmentRepository.findAllByOrderByWeekAscEndDateAsc()
                : assignmentRepository.findAllByPart_IdOrderByWeekAscEndDateAsc(getStaffPart(staffMemberId).getId());
        return toStaffDetailWeekGroups(assignments);
    }

    /**
     * 제출을 승인/반려로 평가한다. STAFF/ADMIN/PRESIDENT 모두 본인 파트의 제출만 평가할 수 있다.
     * 승인 시 제출 시각(updatedAt)이 마감일 이전이면 '승인완료', 이후면 '지각제출'로 표시되고
     * (AssignmentSubmitDisplayStatusCalculator가 계산), 반려는 시점과 무관하게 '승인반려'로 표시된다.
     * 평가하면 reviewMember/approvalDate가 갱신되지만, updatedAt(제출 시각)은 평가로 바뀌면 안 되므로 그대로 유지한다.
     */
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN', 'PRESIDENT')")
    @Transactional
    public AssignmentSubmitResponse evaluate(Long staffMemberId, Long assignmentId, Long submitId,
                                              AssignmentSubmitEvaluateRequest request) {
        Assignment assignment = getManageableAssignment(staffMemberId, assignmentId);
        Member staff = getMember(staffMemberId);

        AssignmentSubmit submit = getSubmit(submitId);
        if (!submit.getAssignment().getId().equals(assignmentId)) {
            throw new CustomException(ErrorCode.ASSIGNMENT_SUBMIT_NOT_FOUND, "존재하지 않는 제출입니다. submitId=" + submitId);
        }
        if (request.status() == AssignmentSubmitStatus.PENDING) {
            throw new CustomException(ErrorCode.INVALID_INPUT, "평가 상태는 APPROVED 또는 REJECTED만 가능합니다.");
        }
        if (request.status() == AssignmentSubmitStatus.REJECTED && !StringUtils.hasText(request.rejectionReason())) {
            throw new CustomException(ErrorCode.INVALID_INPUT, "반려 사유를 입력해주세요.");
        }
        String rejectionReason = request.status() == AssignmentSubmitStatus.REJECTED ? request.rejectionReason() : null;

        // submit.approve()/reject()로 엔티티를 직접 고쳐 save하면 JPA auditing이 updatedAt까지 같이 갱신해버리므로,
        // 평가 컬럼만 벌크 UPDATE로 반영하고 refresh()로 다시 읽어들인다 (updatedAt은 DB에 있는 값 그대로 유지됨).
        assignmentSubmitRepository.applyEvaluation(submit.getId(), request.status(), staff, staff.getName(),
                LocalDateTime.now(), rejectionReason);
        entityManager.refresh(submit);

        assignmentPushNotificationService.sendEvaluationNotification(submit);

        LocalDateTime endDate = assignmentIndividualDeadlineRepository
                .findByAssignment_IdAndMember_Id(assignmentId, submit.getSubmitMember().getId())
                .map(AssignmentIndividualDeadline::getDeadline)
                .orElse(assignment.getEndDate());

        List<SubmissionFile> files = submissionFileRepository.findAllByAssignmentSubmit(submit);
        AssignmentSubmitDisplayStatus displayStatus = AssignmentSubmitDisplayStatusCalculator.calculate(endDate, submit);
        return AssignmentSubmitResponse.of(submit, files, displayStatus);
    }

    /**
     * 과제 목록을 주차별로 묶어 파트원 제출 현황을 집계한다. 대상 파트가 하나든 여러 개든(ADMIN/PRESIDENT 전체 조회)
     * 상관없이 과제마다 자기 소속 파트의 파트원으로 집계하므로 그대로 재사용할 수 있다.
     */
    private List<AssignmentStaffSummaryResponse.WeekGroup> toStaffSummaryWeekGroups(List<Assignment> assignments) {
        if (assignments.isEmpty()) {
            return List.of();
        }

        Map<Long, List<Member>> babyLionsByPartId = babyLionsByPartId(assignments);
        StatusLookup statusLookup = buildStatusLookup(assignments);

        Map<Integer, List<Assignment>> assignmentsByWeek = assignments.stream()
                .collect(Collectors.groupingBy(Assignment::getWeek, LinkedHashMap::new, Collectors.toList()));

        return assignmentsByWeek.entrySet().stream()
                .map(entry -> new AssignmentStaffSummaryResponse.WeekGroup(entry.getKey(), entry.getValue().stream()
                        .map(assignment -> toStaffSummary(assignment,
                                babyLionsByPartId.getOrDefault(assignment.getPart().getId(), List.of()), statusLookup))
                        .toList()))
                .toList();
    }

    /**
     * {@link #toStaffSummaryWeekGroups}의 상세 버전. 과제별 파트원 전체의 개별 제출 상태(제출물 포함)까지 내려준다.
     */
    private List<AssignmentStaffDetailResponse.WeekGroup> toStaffDetailWeekGroups(List<Assignment> assignments) {
        if (assignments.isEmpty()) {
            return List.of();
        }

        Map<Long, List<Member>> babyLionsByPartId = babyLionsByPartId(assignments);
        StatusLookup statusLookup = buildStatusLookup(assignments);
        Map<Long, List<SubmissionFile>> filesBySubmitId = groupFilesBySubmitId(
                statusLookup.latestByAssignmentIdThenMemberId().values().stream()
                        .flatMap(byMemberId -> byMemberId.values().stream())
                        .toList());

        Map<Integer, List<Assignment>> assignmentsByWeek = assignments.stream()
                .collect(Collectors.groupingBy(Assignment::getWeek, LinkedHashMap::new, Collectors.toList()));

        return assignmentsByWeek.entrySet().stream()
                .map(entry -> new AssignmentStaffDetailResponse.WeekGroup(entry.getKey(), entry.getValue().stream()
                        .map(assignment -> toStaffDetail(assignment,
                                babyLionsByPartId.getOrDefault(assignment.getPart().getId(), List.of()), statusLookup, filesBySubmitId))
                        .toList()))
                .toList();
    }

    /** 주어진 과제들이 속한 파트들의 아기사자 목록을 파트 ID로 묶어서 한 번에 조회한다. */
    private Map<Long, List<Member>> babyLionsByPartId(List<Assignment> assignments) {
        List<Long> partIds = assignments.stream().map(assignment -> assignment.getPart().getId()).distinct().toList();
        return memberRepository.findByPart_IdInAndRole(partIds, MemberRole.BABY_LION).stream()
                .collect(Collectors.groupingBy(member -> member.getPart().getId()));
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

    /** 조회용: STAFF는 본인 파트 과제만, ADMIN/PRESIDENT는 파트 제한 없이 대상 과제를 가져온다. */
    private Assignment getViewableAssignment(Long staffMemberId, Long assignmentId) {
        Assignment assignment = getAssignment(assignmentId);

        if (!isOwnPartOrAdminPresident(staffMemberId, assignment.getPart().getId())) {
            throw new CustomException(ErrorCode.ASSIGNMENT_PART_MISMATCH, "본인 파트의 과제만 관리할 수 있습니다. assignmentId=" + assignmentId);
        }

        return assignment;
    }

    /** 관리용(수정/삭제/개별 마감일/평가): 과제 생성과 동일하게 STAFF/ADMIN/PRESIDENT 모두 예외 없이 본인 소속 파트의 과제만 대상으로 할 수 있다. */
    private Assignment getManageableAssignment(Long staffMemberId, Long assignmentId) {
        Assignment assignment = getAssignment(assignmentId);

        Part staffPart = getStaffPart(staffMemberId);
        if (!assignment.getPart().getId().equals(staffPart.getId())) {
            throw new CustomException(ErrorCode.ASSIGNMENT_PART_MISMATCH, "본인 파트의 과제만 관리할 수 있습니다. assignmentId=" + assignmentId);
        }

        return assignment;
    }

    /** ADMIN/PRESIDENT면 파트 무관 통과, 그 외(STAFF)는 targetPartId가 본인 소속 파트와 같아야 통과. */
    private boolean isOwnPartOrAdminPresident(Long staffMemberId, Long targetPartId) {
        return isAdminOrPresident(staffMemberId) || getStaffPart(staffMemberId).getId().equals(targetPartId);
    }

    private boolean isAdminOrPresident(Long memberId) {
        MemberRole role = getMember(memberId).getRole();
        return role == MemberRole.PRESIDENT || role == MemberRole.ADMIN;
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

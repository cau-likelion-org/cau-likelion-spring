package com.example.cau_likelion_spring.mypage.service;

import com.example.cau_likelion_spring.assignment.domain.Assignment;
import com.example.cau_likelion_spring.assignment.domain.AssignmentIndividualDeadline;
import com.example.cau_likelion_spring.assignment.domain.AssignmentSubmit;
import com.example.cau_likelion_spring.assignment.domain.AssignmentSubmitDisplayStatus;
import com.example.cau_likelion_spring.assignment.domain.AssignmentSubmitDisplayStatusCalculator;
import com.example.cau_likelion_spring.assignment.repository.AssignmentIndividualDeadlineRepository;
import com.example.cau_likelion_spring.assignment.repository.AssignmentRepository;
import com.example.cau_likelion_spring.assignment.repository.AssignmentSubmitRepository;
import com.example.cau_likelion_spring.attendance.domain.AttendanceStatus;
import com.example.cau_likelion_spring.attendance.repository.DetailAttendanceRepository;
import com.example.cau_likelion_spring.global.exception.CustomException;
import com.example.cau_likelion_spring.global.exception.ErrorCode;
import com.example.cau_likelion_spring.member.domain.Member;
import com.example.cau_likelion_spring.member.domain.MemberRole;
import com.example.cau_likelion_spring.member.repository.MemberRepository;
import com.example.cau_likelion_spring.mypage.dto.MemberScoreResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 마이페이지 상벌점 내역 조회. 대상이 1명이든 여러 명이든 항상 배치로 조회해서
 * 인원수와 무관하게 쿼리 개수가 고정되도록 한다 (N+1 방지).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberScoreService {

    private final MemberRepository memberRepository;
    private final DetailAttendanceRepository detailAttendanceRepository;
    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmitRepository assignmentSubmitRepository;
    private final AssignmentIndividualDeadlineRepository assignmentIndividualDeadlineRepository;

    /** 아기사자 본인 상벌점 조회 */
    public MemberScoreResponse getMyScore(Long memberId) {
        Member member = getMember(memberId);
        return calculateScores(List.of(member)).get(0);
    }

    /**
     * 운영진/회장/admin의 상벌점 목록 조회.
     * 운영진은 본인 파트 아기사자만, 회장/admin은 전체 아기사자를 조회한다.
     * 기수를 별도로 구분하지 않는 이유 - 기수가 끝나면 운영진이 그 기수 아기사자의 role을 수동으로 바꿔주므로,
     * role=BABY_LION 자체가 이미 "현재 기수"로 자연히 걸러진 상태다.
     */
    public List<MemberScoreResponse> getScores(Long requesterId) {
        Member requester = getMember(requesterId);

        List<Member> babyLions = requester.getRole() == MemberRole.STAFF
                ? memberRepository.findByPart_IdAndRole(requester.getPart().getId(), MemberRole.BABY_LION)
                : memberRepository.findByRole(MemberRole.BABY_LION);

        return calculateScores(babyLions);
    }

    private List<MemberScoreResponse> calculateScores(List<Member> members) {
        if (members.isEmpty()) {
            return List.of();
        }

        List<Long> memberIds = members.stream().map(Member::getId).toList();
        Map<Long, Map<AttendanceStatus, Long>> attendanceCountsByMemberId = findAttendanceCountsByMemberId(memberIds);

        List<Long> partIds = members.stream().map(m -> m.getPart().getId()).distinct().toList();
        List<Assignment> assignments = assignmentRepository.findAllByPart_IdIn(partIds);
        Map<Long, List<Assignment>> assignmentsByPartId = assignments.stream()
                .collect(Collectors.groupingBy(a -> a.getPart().getId()));

        List<Long> assignmentIds = assignments.stream().map(Assignment::getId).toList();
        Map<Long, Map<Long, AssignmentSubmit>> latestSubmitByAssignmentIdThenMemberId =
                findLatestSubmitsByAssignmentIdThenMemberId(assignmentIds);
        Map<Long, Map<Long, LocalDateTime>> individualDeadlineByAssignmentIdThenMemberId =
                findIndividualDeadlinesByAssignmentIdThenMemberId(assignmentIds);

        return members.stream()
                .map(member -> toScoreResponse(member,
                        attendanceCountsByMemberId.getOrDefault(member.getId(), Map.of()),
                        assignmentsByPartId.getOrDefault(member.getPart().getId(), List.of()),
                        latestSubmitByAssignmentIdThenMemberId,
                        individualDeadlineByAssignmentIdThenMemberId))
                .toList();
    }

    private MemberScoreResponse toScoreResponse(Member member,
                                                 Map<AttendanceStatus, Long> attendanceCounts,
                                                 List<Assignment> partAssignments,
                                                 Map<Long, Map<Long, AssignmentSubmit>> latestSubmitByAssignmentIdThenMemberId,
                                                 Map<Long, Map<Long, LocalDateTime>> individualDeadlineByAssignmentIdThenMemberId) {
        long lateCount = attendanceCounts.getOrDefault(AttendanceStatus.LATE, 0L);
        long absentCount = attendanceCounts.getOrDefault(AttendanceStatus.ABSENT, 0L);
        long unauthorizedAbsentCount = attendanceCounts.getOrDefault(AttendanceStatus.UNAUTHORIZED_ABSENT, 0L);

        long lateSubmitCount = 0;
        long missedCount = 0;
        for (Assignment assignment : partAssignments) {
            AssignmentSubmit latest = latestSubmitByAssignmentIdThenMemberId
                    .getOrDefault(assignment.getId(), Map.of())
                    .get(member.getId());
            LocalDateTime endDate = individualDeadlineByAssignmentIdThenMemberId
                    .getOrDefault(assignment.getId(), Map.of())
                    .getOrDefault(member.getId(), assignment.getEndDate());

            AssignmentSubmitDisplayStatus status = AssignmentSubmitDisplayStatusCalculator.calculate(endDate, latest);
            if (status == AssignmentSubmitDisplayStatus.MISSED) {
                missedCount++;
            } else if (status == AssignmentSubmitDisplayStatus.LATE_SUBMITTED) {
                lateSubmitCount++;
            }
        }

        return MemberScoreResponse.of(member, lateCount, absentCount, unauthorizedAbsentCount, lateSubmitCount, missedCount);
    }

    private Map<Long, Map<AttendanceStatus, Long>> findAttendanceCountsByMemberId(List<Long> memberIds) {
        return detailAttendanceRepository.countByStatusForMembers(memberIds).stream()
                .collect(Collectors.groupingBy(DetailAttendanceRepository.MemberStatusCount::getMemberId,
                        Collectors.toMap(DetailAttendanceRepository.MemberStatusCount::getStatus,
                                DetailAttendanceRepository.MemberStatusCount::getCount)));
    }

    private Map<Long, Map<Long, AssignmentSubmit>> findLatestSubmitsByAssignmentIdThenMemberId(List<Long> assignmentIds) {
        return assignmentSubmitRepository.findAllByAssignment_IdInOrderByCreatedAtDesc(assignmentIds).stream()
                .collect(Collectors.groupingBy(submit -> submit.getAssignment().getId(),
                        Collectors.toMap(submit -> submit.getSubmitMember().getId(), Function.identity(),
                                (firstByCreatedAtDesc, ignoredOlder) -> firstByCreatedAtDesc)));
    }

    private Map<Long, Map<Long, LocalDateTime>> findIndividualDeadlinesByAssignmentIdThenMemberId(List<Long> assignmentIds) {
        return assignmentIndividualDeadlineRepository.findAllByAssignment_IdIn(assignmentIds).stream()
                .collect(Collectors.groupingBy(deadline -> deadline.getAssignment().getId(),
                        Collectors.toMap(deadline -> deadline.getMember().getId(),
                                AssignmentIndividualDeadline::getDeadline)));
    }

    private Member getMember(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND, "존재하지 않는 구성원입니다. id=" + id));
    }
}

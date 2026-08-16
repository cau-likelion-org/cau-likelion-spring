package com.example.cau_likelion_spring.attendance.service;

import com.example.cau_likelion_spring.attendance.domain.AttendanceStatus;
import com.example.cau_likelion_spring.attendance.domain.DetailAttendance;
import com.example.cau_likelion_spring.attendance.domain.WeeklyAttendance;
import com.example.cau_likelion_spring.attendance.dto.AttendanceCheckRequest;
import com.example.cau_likelion_spring.attendance.dto.AttendanceStatusBatchUpdateRequest;
import com.example.cau_likelion_spring.attendance.dto.AttendanceStatusResponse;
import com.example.cau_likelion_spring.attendance.dto.MemberAttendanceResponse;
import com.example.cau_likelion_spring.attendance.dto.WeeklyAttendanceCreateRequest;
import com.example.cau_likelion_spring.attendance.dto.WeeklyAttendanceResponse;
import com.example.cau_likelion_spring.attendance.repository.DetailAttendanceRepository;
import com.example.cau_likelion_spring.attendance.repository.WeeklyAttendanceRepository;
import com.example.cau_likelion_spring.global.exception.CustomException;
import com.example.cau_likelion_spring.global.exception.ErrorCode;
import com.example.cau_likelion_spring.member.domain.Member;
import com.example.cau_likelion_spring.member.domain.MemberRole;
import com.example.cau_likelion_spring.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceService {

    private final DetailAttendanceRepository detailAttendanceRepository;
    private final WeeklyAttendanceRepository weeklyAttendanceRepository;
    private final MemberRepository memberRepository;

    /** 세션 시작 시각 */
    @Value("${attendance.session-start-hour}")
    private int sessionStartHour;

    /** 정상 출석으로 인정되는 유예 시간(분). 세션 시작 후 이 시간까지는 출석, 이후는 지각 */
    @Value("${attendance.late-grace-minutes}")
    private int lateGraceMinutes;

    /** 출석 체크가 가능한 마감 시각 */
    @Value("${attendance.check-close-hour}")
    private int checkCloseHour;

    @PreAuthorize("hasRole('PRESIDENT')")
    @Transactional
    public WeeklyAttendanceResponse createWeeklyAttendance(WeeklyAttendanceCreateRequest request) {
        if (weeklyAttendanceRepository.existsByDate(request.date())) {
            throw new CustomException(ErrorCode.DUPLICATE_WEEKLY_ATTENDANCE,
                    "해당 날짜에 이미 출석부가 존재합니다. date=" + request.date());
        }

        WeeklyAttendance weeklyAttendance = weeklyAttendanceRepository.save(WeeklyAttendance.builder()
                .date(request.date())
                .password(request.password())
                .weekNumber(request.weekNumber())
                .build());

        List<Member> babyLions = memberRepository.findByRole(MemberRole.BABY_LION);
        List<DetailAttendance> detailAttendances = babyLions.stream()
                .map(babyLion -> DetailAttendance.builder()
                        .member(babyLion)
                        .weeklyAttendance(weeklyAttendance)
                        .status(AttendanceStatus.BEFORE)
                        .build())
                .toList();
        detailAttendanceRepository.saveAll(detailAttendances);

        return WeeklyAttendanceResponse.from(weeklyAttendance);
    }

    @PreAuthorize("hasRole('BABY_LION')")
    @Transactional
    public AttendanceStatusResponse checkAttendance(Long memberId, AttendanceCheckRequest request) {
        LocalDate today = LocalDate.now();

        WeeklyAttendance weeklyAttendance = weeklyAttendanceRepository.findByDate(today)
                .orElseThrow(() -> new CustomException(ErrorCode.WEEKLY_ATTENDANCE_NOT_FOUND,
                        "금일 출석부는 아직 생성되지 않았습니다."));

        if (!weeklyAttendance.getPassword().equals(request.password())) {
            throw new CustomException(ErrorCode.INVALID_ATTENDANCE_PASSWORD);
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(today.atTime(checkCloseHour, 0))) {
            throw new CustomException(ErrorCode.ATTENDANCE_CHECK_CLOSED);
        }

        DetailAttendance detailAttendance = detailAttendanceRepository
                .findByMember_IdAndWeeklyAttendance_Id(memberId, weeklyAttendance.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.DETAIL_ATTENDANCE_NOT_FOUND, "출석 대상이 아닙니다."));

        LocalDateTime lateStandard = today.atTime(sessionStartHour, 0).plusMinutes(lateGraceMinutes);
        AttendanceStatus status = now.isAfter(lateStandard) ? AttendanceStatus.LATE : AttendanceStatus.PRESENT;

        detailAttendance.checkIn(status, now);

        return AttendanceStatusResponse.from(detailAttendance);
    }

    @Transactional
    public void markUnauthorizedAbsences() {
        LocalDate today = LocalDate.now();

        List<DetailAttendance> expired = detailAttendanceRepository
                .findByStatusAndWeeklyAttendance_DateLessThanEqual(AttendanceStatus.BEFORE, today);
        expired.forEach(DetailAttendance::markAsUnauthorizedAbsent);
    }

    @PreAuthorize("hasRole('BABY_LION')")
    public List<AttendanceStatusResponse> getMyAttendances(Long memberId) {
        return detailAttendanceRepository.findByMember_IdOrderByWeeklyAttendance_WeekNumberAsc(memberId).stream()
                .map(AttendanceStatusResponse::from)
                .toList();
    }

    @PreAuthorize("hasRole('STAFF')")
    public List<MemberAttendanceResponse> getPartAttendances(Long staffMemberId) {
        Member staff = getMember(staffMemberId);

        List<Member> babyLions = memberRepository.findByPart_IdAndRole(staff.getPart().getId(), MemberRole.BABY_LION);
        return buildMemberAttendanceResponses(babyLions);
    }

    @PreAuthorize("hasAnyRole('PRESIDENT', 'ADMIN')")
    public List<MemberAttendanceResponse> getAllAttendances() {
        List<Member> babyLions = memberRepository.findByRole(MemberRole.BABY_LION);
        return buildMemberAttendanceResponses(babyLions);
    }

    private List<MemberAttendanceResponse> buildMemberAttendanceResponses(List<Member> babyLions) {
        if (babyLions.isEmpty()) {
            return List.of();
        }

        List<Long> babyLionIds = babyLions.stream().map(Member::getId).toList();
        Map<Long, List<AttendanceStatusResponse>> attendancesByMemberId = detailAttendanceRepository
                .findByMember_IdInOrderByWeeklyAttendance_WeekNumberAsc(babyLionIds).stream()
                .collect(Collectors.groupingBy(
                        detailAttendance -> detailAttendance.getMember().getId(),
                        Collectors.mapping(AttendanceStatusResponse::from, Collectors.toList())
                ));

        return babyLions.stream()
                .map(babyLion -> MemberAttendanceResponse.of(babyLion, attendancesByMemberId.getOrDefault(babyLion.getId(), List.of())))
                .toList();
    }

    @PreAuthorize("hasAnyRole('STAFF', 'PRESIDENT')")
    @Transactional
    public List<AttendanceStatusResponse> updateAttendanceStatuses(Long memberId, AttendanceStatusBatchUpdateRequest request) {
        Member requester = getMember(memberId);

        return request.updates().stream()
                .map(item -> updateAttendanceStatus(requester, item))
                .toList();
    }

    private AttendanceStatusResponse updateAttendanceStatus(Member requester, AttendanceStatusBatchUpdateRequest.AttendanceUpdateItem item) {
        boolean requiresReason = item.status() == AttendanceStatus.ABSENT || item.status() == AttendanceStatus.EXCUSED;
        if (requiresReason && (item.reason() == null || item.reason().isBlank())) {
            throw new CustomException(ErrorCode.ATTENDANCE_REASON_REQUIRED);
        }

        DetailAttendance detailAttendance = detailAttendanceRepository.findById(item.detailAttendanceId())
                .orElseThrow(() -> new CustomException(ErrorCode.DETAIL_ATTENDANCE_NOT_FOUND,
                        "존재하지 않는 출결 기록입니다. id=" + item.detailAttendanceId()));

        boolean isSamePart = detailAttendance.getMember().getPart().getId().equals(requester.getPart().getId());
        if (requester.getRole() == MemberRole.STAFF && !isSamePart) {
            throw new CustomException(ErrorCode.FORBIDDEN, "본인 파트의 아기사자만 수정할 수 있습니다.");
        }

        detailAttendance.updateStatusByStaff(item.status(), item.reason());

        return AttendanceStatusResponse.from(detailAttendance);
    }

    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND, "존재하지 않는 멤버입니다. id=" + memberId));
    }
}

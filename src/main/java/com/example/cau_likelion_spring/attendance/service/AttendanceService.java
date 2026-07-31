package com.example.cau_likelion_spring.attendance.service;

import com.example.cau_likelion_spring.attendance.domain.AttendanceStatus;
import com.example.cau_likelion_spring.attendance.domain.DetailAttendance;
import com.example.cau_likelion_spring.attendance.domain.WeeklyAttendance;
import com.example.cau_likelion_spring.attendance.dto.AttendanceCheckRequest;
import com.example.cau_likelion_spring.attendance.dto.AttendanceStatusResponse;
import com.example.cau_likelion_spring.attendance.dto.MemberAttendanceResponse;
import com.example.cau_likelion_spring.attendance.dto.WeeklyAttendanceCreateRequest;
import com.example.cau_likelion_spring.attendance.dto.WeeklyAttendanceResponse;
import com.example.cau_likelion_spring.attendance.exception.AttendanceCheckClosedException;
import com.example.cau_likelion_spring.attendance.exception.AttendanceTargetNotFoundException;
import com.example.cau_likelion_spring.attendance.exception.DuplicateWeeklyAttendanceException;
import com.example.cau_likelion_spring.attendance.exception.InvalidAttendancePasswordException;
import com.example.cau_likelion_spring.attendance.exception.WeeklyAttendanceNotFoundException;
import com.example.cau_likelion_spring.attendance.repository.DetailAttendanceRepository;
import com.example.cau_likelion_spring.attendance.repository.WeeklyAttendanceRepository;
import com.example.cau_likelion_spring.member.domain.Member;
import com.example.cau_likelion_spring.member.domain.MemberRole;
import com.example.cau_likelion_spring.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
            throw new DuplicateWeeklyAttendanceException(request.date());
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
                .orElseThrow(WeeklyAttendanceNotFoundException::new);

        if (!weeklyAttendance.getPassword().equals(request.password())) {
            throw new InvalidAttendancePasswordException();
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(today.atTime(checkCloseHour, 0))) {
            throw new AttendanceCheckClosedException();
        }

        DetailAttendance detailAttendance = detailAttendanceRepository
                .findByMember_IdAndWeeklyAttendance_Id(memberId, weeklyAttendance.getId())
                .orElseThrow(AttendanceTargetNotFoundException::new);

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

    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 멤버입니다."));
    }
}

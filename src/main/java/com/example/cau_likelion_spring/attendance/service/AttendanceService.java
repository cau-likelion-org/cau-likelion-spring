package com.example.cau_likelion_spring.attendance.service;

import com.example.cau_likelion_spring.attendance.domain.AttendanceStatus;
import com.example.cau_likelion_spring.attendance.domain.DetailAttendance;
import com.example.cau_likelion_spring.attendance.domain.WeeklyAttendance;
import com.example.cau_likelion_spring.attendance.dto.AttendanceStatusResponse;
import com.example.cau_likelion_spring.attendance.dto.MemberAttendanceResponse;
import com.example.cau_likelion_spring.attendance.dto.WeeklyAttendanceCreateRequest;
import com.example.cau_likelion_spring.attendance.dto.WeeklyAttendanceResponse;
import com.example.cau_likelion_spring.attendance.exception.DuplicateWeeklyAttendanceException;
import com.example.cau_likelion_spring.attendance.repository.DetailAttendanceRepository;
import com.example.cau_likelion_spring.attendance.repository.WeeklyAttendanceRepository;
import com.example.cau_likelion_spring.member.domain.Member;
import com.example.cau_likelion_spring.member.domain.MemberRole;
import com.example.cau_likelion_spring.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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

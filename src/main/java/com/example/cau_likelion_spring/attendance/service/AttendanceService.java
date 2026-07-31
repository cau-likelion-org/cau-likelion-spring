package com.example.cau_likelion_spring.attendance.service;

import com.example.cau_likelion_spring.attendance.dto.AttendanceStatusResponse;
import com.example.cau_likelion_spring.attendance.dto.MemberAttendanceResponse;
import com.example.cau_likelion_spring.attendance.repository.DetailAttendanceRepository;
import com.example.cau_likelion_spring.global.exception.CustomException;
import com.example.cau_likelion_spring.global.exception.ErrorCode;
import com.example.cau_likelion_spring.member.domain.Member;
import com.example.cau_likelion_spring.member.domain.MemberRole;
import com.example.cau_likelion_spring.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceService {

    private final DetailAttendanceRepository detailAttendanceRepository;
    private final MemberRepository memberRepository;

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
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND, "존재하지 않는 멤버입니다. id=" + memberId));
    }
}

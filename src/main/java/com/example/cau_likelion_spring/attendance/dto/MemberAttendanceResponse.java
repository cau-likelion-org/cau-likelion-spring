package com.example.cau_likelion_spring.attendance.dto;

import com.example.cau_likelion_spring.attendance.domain.AttendanceStatus;
import com.example.cau_likelion_spring.member.domain.Member;
import com.example.cau_likelion_spring.mypage.domain.MemberScoreCalculator;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record MemberAttendanceResponse(

        @Schema(description = "아기사자 멤버 ID")
        Long memberId,

        @Schema(description = "아기사자 이름")
        String memberName,

        @Schema(description = "기수 번호")
        Integer generationNumber,

        @Schema(description = "파트 이름")
        String partName,

        @Schema(description = "주차별 출결 현황")
        List<AttendanceStatusResponse> attendances,

        @Schema(description = "출결로 인한 감점 (과제 감점 제외)")
        double attendancePenalty
) {

    public static MemberAttendanceResponse of(Member member, List<AttendanceStatusResponse> attendances) {
        long lateCount = countByStatus(attendances, AttendanceStatus.LATE);
        long absentCount = countByStatus(attendances, AttendanceStatus.ABSENT);
        long unauthorizedAbsentCount = countByStatus(attendances, AttendanceStatus.UNAUTHORIZED_ABSENT);
        double attendancePenalty = MemberScoreCalculator.calculateAttendancePenalty(lateCount, absentCount, unauthorizedAbsentCount);

        return new MemberAttendanceResponse(
                member.getId(),
                member.getName(),
                member.getPart().getGeneration().getNumber(),
                member.getPart().getName(),
                attendances,
                attendancePenalty
        );
    }

    private static long countByStatus(List<AttendanceStatusResponse> attendances, AttendanceStatus status) {
        return attendances.stream().filter(a -> a.status() == status).count();
    }
}

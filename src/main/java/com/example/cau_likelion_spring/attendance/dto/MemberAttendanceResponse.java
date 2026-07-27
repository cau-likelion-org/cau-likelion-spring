package com.example.cau_likelion_spring.attendance.dto;

import com.example.cau_likelion_spring.member.domain.Member;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record MemberAttendanceResponse(

        @Schema(description = "아기사자 멤버 ID")
        Long memberId,

        @Schema(description = "아기사자 이름")
        String memberName,

        @Schema(description = "기수 번호")
        Integer generationNumber,

        @Schema(description = "주차별 출결 현황")
        List<AttendanceStatusResponse> attendances
) {

    public static MemberAttendanceResponse of(Member member, List<AttendanceStatusResponse> attendances) {
        return new MemberAttendanceResponse(
                member.getId(),
                member.getName(),
                member.getPart().getGeneration().getNumber(),
                attendances
        );
    }
}

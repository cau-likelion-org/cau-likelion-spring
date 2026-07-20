package com.example.cau_likelion_spring.attendance.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AttendanceStatus {

    BEFORE("출석 전"),
    PRESENT("출석"),
    LATE("지각"),
    ABSENT("결석"),
    UNAUTHORIZED_ABSENT("무단결석"),
    EXCUSED("공결");

    private final String description;
}
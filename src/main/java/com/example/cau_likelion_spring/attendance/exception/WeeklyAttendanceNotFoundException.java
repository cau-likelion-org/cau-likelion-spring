package com.example.cau_likelion_spring.attendance.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class WeeklyAttendanceNotFoundException extends ResponseStatusException {

    public WeeklyAttendanceNotFoundException() {
        super(HttpStatus.NOT_FOUND, "금일 출석부는 아직 생성되지 않았습니다.");
    }
}

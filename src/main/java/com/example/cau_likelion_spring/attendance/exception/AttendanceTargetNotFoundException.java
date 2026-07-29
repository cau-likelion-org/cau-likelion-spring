package com.example.cau_likelion_spring.attendance.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class AttendanceTargetNotFoundException extends ResponseStatusException {

    public AttendanceTargetNotFoundException() {
        super(HttpStatus.NOT_FOUND, "출석 대상이 아닙니다.");
    }
}

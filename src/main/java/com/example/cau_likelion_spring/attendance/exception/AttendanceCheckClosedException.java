package com.example.cau_likelion_spring.attendance.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class AttendanceCheckClosedException extends ResponseStatusException {

    public AttendanceCheckClosedException() {
        super(HttpStatus.BAD_REQUEST, "출석 가능한 시간이 아닙니다.");
    }
}

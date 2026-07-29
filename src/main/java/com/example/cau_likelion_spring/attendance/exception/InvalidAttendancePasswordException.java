package com.example.cau_likelion_spring.attendance.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class InvalidAttendancePasswordException extends ResponseStatusException {

    public InvalidAttendancePasswordException() {
        super(HttpStatus.BAD_REQUEST, "비밀번호가 올바르지 않습니다.");
    }
}

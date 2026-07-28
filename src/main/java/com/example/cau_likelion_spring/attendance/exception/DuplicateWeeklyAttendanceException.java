package com.example.cau_likelion_spring.attendance.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

public class DuplicateWeeklyAttendanceException extends ResponseStatusException {

    public DuplicateWeeklyAttendanceException(LocalDate date) {
        super(HttpStatus.CONFLICT, "해당 날짜에 이미 출석부가 존재합니다. date=" + date);
    }
}

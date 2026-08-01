package com.example.cau_likelion_spring.attendance.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class AttendanceReasonRequiredException extends ResponseStatusException {

    public AttendanceReasonRequiredException() {
        super(HttpStatus.BAD_REQUEST, "결석 또는 공결로 변경할 때는 사유를 입력해야 합니다.");
    }
}

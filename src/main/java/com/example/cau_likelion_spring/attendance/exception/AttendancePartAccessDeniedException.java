package com.example.cau_likelion_spring.attendance.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class AttendancePartAccessDeniedException extends ResponseStatusException {

    public AttendancePartAccessDeniedException() {
        super(HttpStatus.FORBIDDEN, "본인 파트의 아기사자만 수정할 수 있습니다.");
    }
}

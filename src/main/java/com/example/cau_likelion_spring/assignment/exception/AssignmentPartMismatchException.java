package com.example.cau_likelion_spring.assignment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class AssignmentPartMismatchException extends ResponseStatusException {

    public AssignmentPartMismatchException(Long assignmentId) {
        super(HttpStatus.FORBIDDEN, "본인 파트의 과제만 관리할 수 있습니다. assignmentId=" + assignmentId);
    }
}

package com.example.cau_likelion_spring.assignment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class AssignmentAlreadyApprovedException extends ResponseStatusException {

    public AssignmentAlreadyApprovedException(Long assignmentId) {
        super(HttpStatus.CONFLICT, "이미 승인된 과제는 다시 제출할 수 없습니다. assignmentId=" + assignmentId);
    }
}

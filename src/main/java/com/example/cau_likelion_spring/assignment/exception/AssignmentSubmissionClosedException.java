package com.example.cau_likelion_spring.assignment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class AssignmentSubmissionClosedException extends ResponseStatusException {

    public AssignmentSubmissionClosedException(Long assignmentId) {
        super(HttpStatus.CONFLICT, "제출 가능 기한이 지나 더 이상 제출할 수 없습니다. assignmentId=" + assignmentId);
    }
}

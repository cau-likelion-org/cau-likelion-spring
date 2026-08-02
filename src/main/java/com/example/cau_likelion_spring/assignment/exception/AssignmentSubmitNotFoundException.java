package com.example.cau_likelion_spring.assignment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class AssignmentSubmitNotFoundException extends ResponseStatusException {

    public AssignmentSubmitNotFoundException(Long submitId) {
        super(HttpStatus.NOT_FOUND, "존재하지 않는 제출입니다. submitId=" + submitId);
    }
}

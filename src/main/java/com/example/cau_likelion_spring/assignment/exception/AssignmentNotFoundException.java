package com.example.cau_likelion_spring.assignment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class AssignmentNotFoundException extends ResponseStatusException {

    public AssignmentNotFoundException(Long id) {
        super(HttpStatus.NOT_FOUND, "존재하지 않는 과제입니다. id=" + id);
    }
}

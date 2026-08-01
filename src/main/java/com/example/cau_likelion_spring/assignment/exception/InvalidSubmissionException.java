package com.example.cau_likelion_spring.assignment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class InvalidSubmissionException extends ResponseStatusException {

    public InvalidSubmissionException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}

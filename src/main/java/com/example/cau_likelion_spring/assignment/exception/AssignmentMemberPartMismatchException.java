package com.example.cau_likelion_spring.assignment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class AssignmentMemberPartMismatchException extends ResponseStatusException {

    public AssignmentMemberPartMismatchException(Long memberId) {
        super(HttpStatus.FORBIDDEN, "과제 파트에 속하지 않은 아기사자입니다. memberId=" + memberId);
    }
}

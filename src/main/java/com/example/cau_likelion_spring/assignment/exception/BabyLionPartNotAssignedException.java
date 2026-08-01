package com.example.cau_likelion_spring.assignment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class BabyLionPartNotAssignedException extends ResponseStatusException {

    public BabyLionPartNotAssignedException(Long memberId) {
        super(HttpStatus.CONFLICT, "아기사자에게 배정된 파트가 없습니다. memberId=" + memberId);
    }
}

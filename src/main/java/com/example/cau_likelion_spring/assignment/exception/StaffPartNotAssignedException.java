package com.example.cau_likelion_spring.assignment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class StaffPartNotAssignedException extends ResponseStatusException {

    public StaffPartNotAssignedException(Long staffMemberId) {
        super(HttpStatus.CONFLICT, "운영진에게 배정된 파트가 없습니다. memberId=" + staffMemberId);
    }
}

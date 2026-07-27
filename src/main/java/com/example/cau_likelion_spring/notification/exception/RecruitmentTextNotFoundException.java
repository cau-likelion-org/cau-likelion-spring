package com.example.cau_likelion_spring.notification.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class RecruitmentTextNotFoundException extends ResponseStatusException {

    public RecruitmentTextNotFoundException(Long id) {
        super(HttpStatus.NOT_FOUND, "존재하지 않는 모집 공고입니다. id=" + id);
    }
}

package com.example.cau_likelion_spring.notification.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class RecruitmentTextAlreadySentException extends ResponseStatusException {

    public RecruitmentTextAlreadySentException(Long id) {
        super(HttpStatus.CONFLICT, "이미 발송이 시작된 공고는 수정/삭제할 수 없습니다. id=" + id);
    }
}

package com.example.cau_likelion_spring.notification.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class DuplicateSubscriptionException extends ResponseStatusException {

    public DuplicateSubscriptionException(String email) {
        super(HttpStatus.CONFLICT, "이미 구독 중인 이메일입니다. email=" + email);
    }
}

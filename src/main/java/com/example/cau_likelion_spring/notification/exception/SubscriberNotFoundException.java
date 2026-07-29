package com.example.cau_likelion_spring.notification.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

public class SubscriberNotFoundException extends ResponseStatusException {

    public SubscriberNotFoundException(List<Long> missingIds) {
        super(HttpStatus.NOT_FOUND, "존재하지 않는 구독자입니다. ids=" + missingIds);
    }
}

package com.example.cau_likelion_spring.member.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class MemberNotFoundException extends ResponseStatusException {

    public MemberNotFoundException(Long id) {
        super(HttpStatus.NOT_FOUND, "존재하지 않는 구성원입니다. id=" + id);
    }
}

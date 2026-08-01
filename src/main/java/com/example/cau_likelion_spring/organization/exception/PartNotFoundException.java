package com.example.cau_likelion_spring.organization.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class PartNotFoundException extends ResponseStatusException {

    public PartNotFoundException(Long id) {
        super(HttpStatus.NOT_FOUND, "존재하지 않는 파트입니다. id=" + id);
    }
}

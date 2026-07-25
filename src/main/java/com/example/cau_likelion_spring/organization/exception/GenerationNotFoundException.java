package com.example.cau_likelion_spring.organization.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class GenerationNotFoundException extends ResponseStatusException {

    public GenerationNotFoundException(Long id) {
        super(HttpStatus.NOT_FOUND, "존재하지 않는 기수입니다. id=" + id);
    }
}

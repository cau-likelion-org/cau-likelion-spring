package com.example.cau_likelion_spring.project.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ProjectNotFoundException extends ResponseStatusException {

    public ProjectNotFoundException(Long id) {
        super(HttpStatus.NOT_FOUND, "존재하지 않는 프로젝트입니다. id=" + id);
    }
}

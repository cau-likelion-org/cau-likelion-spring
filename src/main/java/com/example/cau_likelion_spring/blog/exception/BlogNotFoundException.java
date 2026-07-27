package com.example.cau_likelion_spring.blog.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class BlogNotFoundException extends ResponseStatusException {

    public BlogNotFoundException(Long id) {
        super(HttpStatus.NOT_FOUND, "존재하지 않는 블로그입니다. id=" + id);
    }
}

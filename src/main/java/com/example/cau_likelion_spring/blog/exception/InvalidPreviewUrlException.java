package com.example.cau_likelion_spring.blog.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class InvalidPreviewUrlException extends ResponseStatusException {

    public InvalidPreviewUrlException(String url) {
        super(HttpStatus.BAD_REQUEST, "유효하지 않은 URL입니다. url=" + url);
    }
}

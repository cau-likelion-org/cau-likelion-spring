package com.example.cau_likelion_spring.blog.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class LinkPreviewFetchException extends ResponseStatusException {

    public LinkPreviewFetchException(String url) {
        super(HttpStatus.BAD_GATEWAY, "블로그 페이지를 불러올 수 없습니다. url=" + url);
    }
}

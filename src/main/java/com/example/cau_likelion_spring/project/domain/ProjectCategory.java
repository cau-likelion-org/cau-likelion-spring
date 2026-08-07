package com.example.cau_likelion_spring.project.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProjectCategory {

    IDEATHON("아이디어톤"),
    HACKATHON("해커톤"),
    CHUNGKATHON("중커톤"),
    ETC("기타");

    private final String description;
}

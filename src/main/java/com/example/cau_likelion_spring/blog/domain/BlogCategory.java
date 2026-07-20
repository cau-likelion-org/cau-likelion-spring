package com.example.cau_likelion_spring.blog.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BlogCategory {

    ACTIVITY_REVIEW("활동 후기"),
    PROJECT_REVIEW("프로젝트 회고"),
    CAREER("인턴·취업"),
    ETC("기타 후기");

    private final String description;
}
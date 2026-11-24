package com.example.cau_likelion_spring.recruitment.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 모집을 알게 된 경로 (Application.source)
 * DB에는 enum 이름이 저장되고, label은 화면 표시용이다.
 */
@Getter
@RequiredArgsConstructor
public enum ApplicationSource {
    HOMEPAGE("멋사 중앙대 홈페이지"),
    INSTAGRAM("인스타그램"),
    EVERYTIME("에브리타임"),
    REFERRAL("지인 추천"),
    ETC("기타");

    private final String label;
}

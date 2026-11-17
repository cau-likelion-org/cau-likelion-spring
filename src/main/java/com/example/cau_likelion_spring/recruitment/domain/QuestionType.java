package com.example.cau_likelion_spring.recruitment.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 2단계 문항 종류 (InterviewQuestion.type)
 * 장문·링크·파일은 InterviewScript, 객관식은 InterviewOption + Options에 내용을 담는다.
 * DB에는 enum 이름이 저장되고, label은 화면 표시용이다.
 */
@Getter
@RequiredArgsConstructor
public enum QuestionType {
    LONG_TEXT("장문"),
    LINK("링크"),
    FILE("파일"),                       // PDF 1개, 30MB 이하
    SINGLE_CHOICE("객관식 단일"),       // 라디오
    MULTIPLE_CHOICE("객관식 복수");     // 체크박스

    private final String label;
}

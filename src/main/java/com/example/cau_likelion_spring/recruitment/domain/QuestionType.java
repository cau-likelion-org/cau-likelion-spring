package com.example.cau_likelion_spring.recruitment.domain;

/**
 * 2단계 문항 종류 (InterviewQuestion.type)
 * 장문·링크·파일은 InterviewScript, 객관식은 InterviewOption + Options에 내용을 담는다.
 */
public enum QuestionType {
    LONG_TEXT,        // 장문
    LINK,             // 링크 (URL)
    FILE,             // 파일 (PDF 1개, 30MB 이하)
    SINGLE_CHOICE,    // 객관식 단일 (라디오)
    MULTIPLE_CHOICE   // 객관식 복수 (체크박스)
}

package com.example.cau_likelion_spring.recruitment.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 결과 안내 문구 개인화 변수.
 * 운영진 화면에서는 버튼으로만 삽입하고, 템플릿 본문에는 token 문자열 그대로 저장된다.
 * 지원자에게 보여줄 때(미리보기 포함) token을 실제 값으로 바꾼다 (ResultMessageRenderer).
 */
@Getter
@RequiredArgsConstructor
public enum ResultTemplateVariable {
    NAME("{이름}", false),
    INTERVIEW_DATE("{면접 일시}", true),
    INTERVIEW_SPOT("{면접 대기 장소}", true);

    private final String token;

    /** 면접 관련 변수 (서류 합격 템플릿에서만 사용 가능) */
    private final boolean interviewOnly;
}

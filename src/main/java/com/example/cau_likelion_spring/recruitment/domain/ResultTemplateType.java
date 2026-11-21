package com.example.cau_likelion_spring.recruitment.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 결과 안내 문구 템플릿 종류 (ResultTemplate.type).
 * 회차마다 종류별로 하나씩 두고, 저장 전에는 defaultText를 기본 문구로 보여준다.
 * 면접 관련 변수({면접 일시}, {면접 대기 장소})는 서류 합격 템플릿에서만 쓸 수 있다.
 */
@Getter
@RequiredArgsConstructor
public enum ResultTemplateType {
    DOCUMENT_PASS("서류 합격", true,
            "{이름}님, 서류 전형에 합격하셨습니다. 축하드립니다!\n\n"
                    + "면접 일시: {면접 일시}\n"
                    + "면접 대기 장소: {면접 대기 장소}\n\n"
                    + "면접 20분 전까지 대기 장소에 도착해주세요."),
    DOCUMENT_FAIL("서류 불합격", false,
            "{이름}님, 지원해주셔서 진심으로 감사드립니다.\n"
                    + "아쉽게도 이번 서류 전형에서는 함께하지 못하게 되었습니다. 보내주신 관심에 깊이 감사드립니다."),
    FINAL_PASS("최종 합격", false,
            "{이름}님, 최종 합격을 축하드립니다!\n"
                    + "앞으로의 활동 안내는 추후 별도로 전달드리겠습니다."),
    FINAL_FAIL("최종 불합격", false,
            "{이름}님, 면접에 참여해주셔서 진심으로 감사드립니다.\n"
                    + "아쉽게도 이번에는 함께하지 못하게 되었습니다. 보내주신 관심에 깊이 감사드립니다.");

    private final String label;

    /** 면접 관련 변수 사용 가능 여부 */
    private final boolean interviewVariablesAllowed;

    private final String defaultText;

    public boolean allows(ResultTemplateVariable variable) {
        return !variable.isInterviewOnly() || interviewVariablesAllowed;
    }
}

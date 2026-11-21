package com.example.cau_likelion_spring.recruitment.service;

import com.example.cau_likelion_spring.recruitment.domain.Applicant;
import com.example.cau_likelion_spring.recruitment.domain.Result;
import com.example.cau_likelion_spring.recruitment.domain.ResultTemplateVariable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * 결과 안내 문구의 개인화 변수를 지원자의 실제 값으로 바꾼다.
 * 백오피스 미리보기와 지원자 결과 화면에서 같이 쓴다.
 * 변수는 한 문구에 여러 번 들어갈 수 있어 모두 바꾸고, 값이 없으면 '미정'으로 채운다.
 */
@Component
public class ResultMessageRenderer {

    private static final String EMPTY_VALUE = "미정";
    private static final DateTimeFormatter INTERVIEW_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy년 M월 d일 (E) HH:mm", Locale.KOREAN);

    /** result는 아직 결과가 입력되지 않은 지원자면 null일 수 있다 */
    public String render(String text, Applicant applicant, Result result) {
        String rendered = text;
        for (ResultTemplateVariable variable : ResultTemplateVariable.values()) {
            rendered = rendered.replace(variable.getToken(), valueOf(variable, applicant, result));
        }
        return rendered;
    }

    private String valueOf(ResultTemplateVariable variable, Applicant applicant, Result result) {
        return switch (variable) {
            case NAME -> orEmpty(applicant.getName());
            case INTERVIEW_DATE -> {
                LocalDateTime date = result == null ? null : result.getFixedInterviewDate();
                yield date == null ? EMPTY_VALUE : date.format(INTERVIEW_DATE_FORMAT);
            }
            case INTERVIEW_SPOT -> orEmpty(result == null ? null : result.getFixedInterviewSpot());
        };
    }

    private String orEmpty(String value) {
        return StringUtils.hasText(value) ? value : EMPTY_VALUE;
    }
}

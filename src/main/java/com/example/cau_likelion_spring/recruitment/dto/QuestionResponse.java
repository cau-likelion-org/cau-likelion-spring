package com.example.cau_likelion_spring.recruitment.dto;

import com.example.cau_likelion_spring.recruitment.domain.InterviewOption;
import com.example.cau_likelion_spring.recruitment.domain.InterviewQuestion;
import com.example.cau_likelion_spring.recruitment.domain.InterviewScript;
import com.example.cau_likelion_spring.recruitment.domain.Options;
import com.example.cau_likelion_spring.recruitment.domain.QuestionType;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 2단계 파트별 문항 조회 응답 (7.4.2).
 * 장문·링크·파일은 InterviewScript, 객관식은 InterviewOption + Options에서 내용을 채운다.
 */
@Getter
@Builder
public class QuestionResponse {

    private Long id;
    private QuestionType type;
    private String question;
    private String detail;
    private Boolean required;
    /** 글자 수 제한 (공백 포함). 장문 계열만 */
    private Integer charLimit;
    /** 객관식 보기. 객관식이 아니면 빈 목록 */
    private List<OptionResponse> options;

    public static QuestionResponse ofScript(InterviewQuestion interviewQuestion, InterviewScript script) {
        return QuestionResponse.builder()
                .id(interviewQuestion.getId())
                .type(interviewQuestion.getType())
                .question(script.getQuestion())
                .detail(script.getDetail())
                .required(interviewQuestion.getRequired())
                .charLimit(script.getLimit())
                .options(List.of())
                .build();
    }

    public static QuestionResponse ofOption(InterviewQuestion interviewQuestion, InterviewOption option,
                                            List<Options> choices) {
        return QuestionResponse.builder()
                .id(interviewQuestion.getId())
                .type(interviewQuestion.getType())
                .question(option.getQuestion())
                .detail(option.getDetail())
                .required(interviewQuestion.getRequired())
                .charLimit(null)
                .options(choices.stream()
                        .map(OptionResponse::from)
                        .toList())
                .build();
    }

    @Getter
    @Builder
    public static class OptionResponse {

        private Long id;
        private String label;

        public static OptionResponse from(Options options) {
            return OptionResponse.builder()
                    .id(options.getId())
                    .label(options.getField())
                    .build();
        }
    }
}

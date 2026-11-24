package com.example.cau_likelion_spring.recruitment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 2단계 문항 답변 하나.
 * 장문·링크·파일은 answer(파일은 업로드된 URL), 객관식은 optionIds로 보낸다.
 */
@Getter
@NoArgsConstructor
public class AnswerRequest {

    @NotNull
    private Long questionId;

    private String answer;

    private List<Long> optionIds;
}

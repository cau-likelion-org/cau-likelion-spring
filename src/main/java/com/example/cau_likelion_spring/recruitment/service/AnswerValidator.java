package com.example.cau_likelion_spring.recruitment.service;

import com.example.cau_likelion_spring.global.exception.CustomException;
import com.example.cau_likelion_spring.global.exception.ErrorCode;
import com.example.cau_likelion_spring.global.util.S3Uploader;
import com.example.cau_likelion_spring.recruitment.domain.InterviewQuestion;
import com.example.cau_likelion_spring.recruitment.domain.Options;
import com.example.cau_likelion_spring.recruitment.domain.QuestionType;
import com.example.cau_likelion_spring.recruitment.dto.AnswerRequest;
import com.example.cau_likelion_spring.recruitment.service.PartQuestionLoader.PartQuestion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 2단계 답변을 파트 문항 기준으로 검증하고, 저장할 값으로 바꾼다 (7.4.2).
 * - 다른 파트 문항이거나 같은 문항에 두 번 답하면 실패
 * - 필수 문항이 비어 있으면 실패, 선택 문항이 비어 있으면 저장하지 않음
 * - 장문: 공백 포함 글자 수 제한 / 링크: http(s) URL / 파일: 우리 버킷 URL
 * - 객관식 단일: 보기 정확히 1개 / 복수: 1개 이상, 중복 없음 / 모두 그 문항의 보기
 * 객관식은 보기 id를 "10,11"처럼 저장한다.
 */
@Component
@RequiredArgsConstructor
public class AnswerValidator {

    private final S3Uploader s3Uploader;

    public List<ValidatedAnswer> validate(List<PartQuestion> partQuestions, List<AnswerRequest> answers) {
        Map<Long, AnswerRequest> answerByQuestionId = indexAnswers(partQuestions, answers);

        List<ValidatedAnswer> validated = new ArrayList<>();
        for (PartQuestion partQuestion : partQuestions) {
            AnswerRequest answer = answerByQuestionId.get(partQuestion.question().getId());
            String value = partQuestion.isChoice()
                    ? toChoiceValue(partQuestion, answer)
                    : toTextValue(partQuestion, answer);

            if (value == null) {
                if (partQuestion.isRequired()) {
                    throw invalid(partQuestion, "필수 문항입니다.");
                }
                continue;
            }
            validated.add(new ValidatedAnswer(partQuestion.question(), value));
        }
        return validated;
    }

    /** 답변을 문항 id로 묶으면서, 다른 파트 문항·중복 답변을 걸러낸다 */
    private Map<Long, AnswerRequest> indexAnswers(List<PartQuestion> partQuestions, List<AnswerRequest> answers) {
        Set<Long> questionIds = partQuestions.stream()
                .map(partQuestion -> partQuestion.question().getId())
                .collect(Collectors.toSet());

        Map<Long, AnswerRequest> answerByQuestionId = new HashMap<>();
        if (answers == null) {
            return answerByQuestionId;
        }
        for (AnswerRequest answer : answers) {
            if (!questionIds.contains(answer.getQuestionId())) {
                throw new CustomException(ErrorCode.RECRUITMENT_INVALID_ANSWER,
                        "선택한 파트의 문항이 아닙니다. questionId=" + answer.getQuestionId());
            }
            if (answerByQuestionId.put(answer.getQuestionId(), answer) != null) {
                throw new CustomException(ErrorCode.RECRUITMENT_INVALID_ANSWER,
                        "같은 문항에 답변이 중복되었습니다. questionId=" + answer.getQuestionId());
            }
        }
        return answerByQuestionId;
    }

    /** 장문·링크·파일 답변. 비어 있으면 null */
    private String toTextValue(PartQuestion partQuestion, AnswerRequest answer) {
        if (answer == null) {
            return null;
        }
        if (answer.getOptionIds() != null && !answer.getOptionIds().isEmpty()) {
            throw invalid(partQuestion, "객관식 문항이 아닙니다.");
        }
        String value = answer.getAnswer();
        if (!StringUtils.hasText(value)) {
            return null;
        }

        QuestionType type = partQuestion.type();
        if (type == QuestionType.LINK && !isHttpUrl(value)) {
            throw invalid(partQuestion, "올바른 URL을 입력해주세요.");
        }
        if (type == QuestionType.FILE && !s3Uploader.isBucketUrl(value)) {
            throw invalid(partQuestion, "업로드된 파일 주소가 아닙니다.");
        }
        Integer limit = partQuestion.script().getLimit();
        if (type == QuestionType.LONG_TEXT && limit != null
                && value.codePointCount(0, value.length()) > limit) {
            throw invalid(partQuestion, "글자 수 제한(" + limit + "자)을 초과했습니다.");
        }
        return value;
    }

    /** 객관식 답변. 선택이 없으면 null, 있으면 보기 id를 쉼표로 이어 반환 */
    private String toChoiceValue(PartQuestion partQuestion, AnswerRequest answer) {
        if (answer == null) {
            return null;
        }
        if (StringUtils.hasText(answer.getAnswer())) {
            throw invalid(partQuestion, "객관식 문항은 보기로만 답할 수 있습니다.");
        }
        List<Long> optionIds = answer.getOptionIds();
        if (optionIds == null || optionIds.isEmpty()) {
            return null;
        }
        if (optionIds.contains(null) || new HashSet<>(optionIds).size() != optionIds.size()) {
            throw invalid(partQuestion, "보기 선택이 올바르지 않습니다.");
        }
        if (partQuestion.type() == QuestionType.SINGLE_CHOICE && optionIds.size() != 1) {
            throw invalid(partQuestion, "보기를 하나만 선택해주세요.");
        }

        Set<Long> choiceIds = partQuestion.choices().stream()
                .map(Options::getId)
                .collect(Collectors.toSet());
        if (!choiceIds.containsAll(optionIds)) {
            throw invalid(partQuestion, "이 문항의 보기가 아닙니다.");
        }

        return optionIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    private boolean isHttpUrl(String value) {
        try {
            URI uri = URI.create(value.trim());
            return ("http".equals(uri.getScheme()) || "https".equals(uri.getScheme()))
                    && StringUtils.hasText(uri.getHost());
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private CustomException invalid(PartQuestion partQuestion, String reason) {
        return new CustomException(ErrorCode.RECRUITMENT_INVALID_ANSWER,
                reason + " questionId=" + partQuestion.question().getId());
    }

    /** 검증을 통과한 답변 (저장할 값) */
    public record ValidatedAnswer(InterviewQuestion question, String value) {
    }
}

package com.example.cau_likelion_spring.recruitment.service;

import com.example.cau_likelion_spring.recruitment.domain.Round;
import com.example.cau_likelion_spring.recruitment.domain.RoundPart;
import com.example.cau_likelion_spring.recruitment.dto.QuestionResponse;
import com.example.cau_likelion_spring.recruitment.service.PartQuestionLoader.PartQuestion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestionService {

    private final CurrentRoundFinder currentRoundFinder;
    private final PartQuestionLoader partQuestionLoader;

    /**
     * 현재 회차의 해당 파트 2단계 문항을 조회한다 (7.4.2).
     * 정렬과 내용 없는 문항 제외 규칙은 PartQuestionLoader를 따른다.
     */
    public List<QuestionResponse> getQuestions(Long roundPartId) {
        Round round = currentRoundFinder.find();
        RoundPart roundPart = currentRoundFinder.findRoundPart(round, roundPartId);

        return partQuestionLoader.load(roundPart).stream()
                .map(this::toResponse)
                .toList();
    }

    private QuestionResponse toResponse(PartQuestion partQuestion) {
        if (partQuestion.isChoice()) {
            return QuestionResponse.ofOption(partQuestion.question(), partQuestion.option(), partQuestion.choices());
        }
        return QuestionResponse.ofScript(partQuestion.question(), partQuestion.script());
    }
}

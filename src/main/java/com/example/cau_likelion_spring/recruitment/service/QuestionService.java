package com.example.cau_likelion_spring.recruitment.service;

import com.example.cau_likelion_spring.global.exception.CustomException;
import com.example.cau_likelion_spring.global.exception.ErrorCode;
import com.example.cau_likelion_spring.recruitment.domain.InterviewOption;
import com.example.cau_likelion_spring.recruitment.domain.InterviewQuestion;
import com.example.cau_likelion_spring.recruitment.domain.InterviewScript;
import com.example.cau_likelion_spring.recruitment.domain.Options;
import com.example.cau_likelion_spring.recruitment.domain.QuestionType;
import com.example.cau_likelion_spring.recruitment.domain.Round;
import com.example.cau_likelion_spring.recruitment.domain.RoundPart;
import com.example.cau_likelion_spring.recruitment.dto.QuestionResponse;
import com.example.cau_likelion_spring.recruitment.repository.InterviewOptionRepository;
import com.example.cau_likelion_spring.recruitment.repository.InterviewQuestionRepository;
import com.example.cau_likelion_spring.recruitment.repository.InterviewScriptRepository;
import com.example.cau_likelion_spring.recruitment.repository.OptionsRepository;
import com.example.cau_likelion_spring.recruitment.repository.RoundPartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestionService {

    private final RoundPartRepository roundPartRepository;
    private final InterviewQuestionRepository interviewQuestionRepository;
    private final InterviewScriptRepository interviewScriptRepository;
    private final InterviewOptionRepository interviewOptionRepository;
    private final OptionsRepository optionsRepository;
    private final CurrentRoundFinder currentRoundFinder;

    /**
     * 현재 회차의 해당 파트 2단계 문항을 조회한다 (7.4.2).
     * 정렬: sequence 오름차순, sequence가 같거나 비어 있으면 만든 순(id).
     * 파트 기준으로 테이블별 한 번씩만 조회해 N+1을 피한다.
     * 내용(Script/Option)이 없는 문항은 응답에서 제외한다.
     */
    public List<QuestionResponse> getQuestions(Long roundPartId) {
        RoundPart roundPart = findRoundPartInCurrentRound(roundPartId);

        // id 순으로 조회한 뒤 sequence로 안정 정렬 -> 같거나 빈 sequence는 만든 순 유지
        List<InterviewQuestion> questions = interviewQuestionRepository.findAllByRoundPartOrderByIdAsc(roundPart)
                .stream()
                .sorted(Comparator.comparing(InterviewQuestion::getSequence,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        Map<Long, InterviewScript> scriptByQuestionId = interviewScriptRepository.findAllByRoundPart(roundPart)
                .stream()
                .filter(script -> script.getInterviewQuestion() != null)
                .collect(Collectors.toMap(script -> script.getInterviewQuestion().getId(),
                        Function.identity(), (first, second) -> first));

        Map<Long, InterviewOption> optionByQuestionId = interviewOptionRepository.findAllByRoundPart(roundPart)
                .stream()
                .filter(option -> option.getInterviewQuestion() != null)
                .collect(Collectors.toMap(option -> option.getInterviewQuestion().getId(),
                        Function.identity(), (first, second) -> first));

        Map<Long, List<Options>> choicesByOptionId = optionsRepository.findAllByRoundPartOrderByIdAsc(roundPart)
                .stream()
                .filter(choice -> choice.getInterviewOption() != null)
                .collect(Collectors.groupingBy(choice -> choice.getInterviewOption().getId()));

        return questions.stream()
                .map(question -> toResponse(question, scriptByQuestionId, optionByQuestionId, choicesByOptionId))
                .filter(Objects::nonNull)
                .toList();
    }

    private QuestionResponse toResponse(InterviewQuestion question,
                                        Map<Long, InterviewScript> scriptByQuestionId,
                                        Map<Long, InterviewOption> optionByQuestionId,
                                        Map<Long, List<Options>> choicesByOptionId) {
        if (isChoice(question.getType())) {
            InterviewOption option = optionByQuestionId.get(question.getId());
            if (option == null) {
                return null;
            }
            return QuestionResponse.ofOption(question, option,
                    choicesByOptionId.getOrDefault(option.getId(), List.of()));
        }

        InterviewScript script = scriptByQuestionId.get(question.getId());
        return script == null ? null : QuestionResponse.ofScript(question, script);
    }

    private boolean isChoice(QuestionType type) {
        return type == QuestionType.SINGLE_CHOICE || type == QuestionType.MULTIPLE_CHOICE;
    }

    /** 요청한 파트가 현재 회차의 파트인지 확인한다 */
    private RoundPart findRoundPartInCurrentRound(Long roundPartId) {
        Round round = currentRoundFinder.find();
        return roundPartRepository.findById(roundPartId)
                .filter(roundPart -> roundPart.getRound() != null
                        && round.getId().equals(roundPart.getRound().getId()))
                .orElseThrow(() -> new CustomException(ErrorCode.RECRUITMENT_ROUND_PART_NOT_FOUND,
                        "현재 회차의 파트가 아닙니다. roundPartId=" + roundPartId));
    }
}

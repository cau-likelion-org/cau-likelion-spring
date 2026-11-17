package com.example.cau_likelion_spring.recruitment.service;

import com.example.cau_likelion_spring.recruitment.domain.InterviewOption;
import com.example.cau_likelion_spring.recruitment.domain.InterviewQuestion;
import com.example.cau_likelion_spring.recruitment.domain.InterviewScript;
import com.example.cau_likelion_spring.recruitment.domain.Options;
import com.example.cau_likelion_spring.recruitment.domain.QuestionType;
import com.example.cau_likelion_spring.recruitment.domain.RoundPart;
import com.example.cau_likelion_spring.recruitment.repository.InterviewOptionRepository;
import com.example.cau_likelion_spring.recruitment.repository.InterviewQuestionRepository;
import com.example.cau_likelion_spring.recruitment.repository.InterviewScriptRepository;
import com.example.cau_likelion_spring.recruitment.repository.OptionsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 파트의 2단계 문항을 내용과 함께 불러온다 (문항 조회·제출 검증에서 공통으로 사용).
 * 정렬: sequence 오름차순, sequence가 같거나 비어 있으면 만든 순(id).
 * 파트 기준으로 테이블별 한 번씩만 조회해 N+1을 피한다.
 * 내용(Script/Option)이 없는 문항은 지원자에게 보이지 않으므로 제외한다.
 */
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PartQuestionLoader {

    private final InterviewQuestionRepository interviewQuestionRepository;
    private final InterviewScriptRepository interviewScriptRepository;
    private final InterviewOptionRepository interviewOptionRepository;
    private final OptionsRepository optionsRepository;

    public List<PartQuestion> load(RoundPart roundPart) {
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
                .map(question -> toPartQuestion(question, scriptByQuestionId, optionByQuestionId, choicesByOptionId))
                .filter(Objects::nonNull)
                .toList();
    }

    private PartQuestion toPartQuestion(InterviewQuestion question,
                                        Map<Long, InterviewScript> scriptByQuestionId,
                                        Map<Long, InterviewOption> optionByQuestionId,
                                        Map<Long, List<Options>> choicesByOptionId) {
        if (PartQuestion.isChoice(question.getType())) {
            InterviewOption option = optionByQuestionId.get(question.getId());
            if (option == null) {
                return null;
            }
            return new PartQuestion(question, null, option,
                    choicesByOptionId.getOrDefault(option.getId(), List.of()));
        }

        InterviewScript script = scriptByQuestionId.get(question.getId());
        return script == null ? null : new PartQuestion(question, script, null, List.of());
    }

    /**
     * 2단계 문항 하나와 그 내용을 묶은 값.
     * 장문·링크·파일(type이 비어 있으면 장문으로 취급)은 script, 객관식은 option + choices가 채워진다.
     */
    public record PartQuestion(InterviewQuestion question, InterviewScript script,
                               InterviewOption option, List<Options> choices) {

        public QuestionType type() {
            return question.getType() == null ? QuestionType.LONG_TEXT : question.getType();
        }

        public boolean isChoice() {
            return isChoice(question.getType());
        }

        public boolean isRequired() {
            return Boolean.TRUE.equals(question.getRequired());
        }

        public static boolean isChoice(QuestionType type) {
            return type == QuestionType.SINGLE_CHOICE || type == QuestionType.MULTIPLE_CHOICE;
        }
    }
}

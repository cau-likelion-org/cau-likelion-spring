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
import com.example.cau_likelion_spring.recruitment.dto.QuestionOrderRequest;
import com.example.cau_likelion_spring.recruitment.dto.QuestionResponse;
import com.example.cau_likelion_spring.recruitment.dto.QuestionSaveRequest;
import com.example.cau_likelion_spring.recruitment.repository.ApplicationRepository;
import com.example.cau_likelion_spring.recruitment.repository.InterviewOptionRepository;
import com.example.cau_likelion_spring.recruitment.repository.InterviewQuestionRepository;
import com.example.cau_likelion_spring.recruitment.repository.InterviewScriptRepository;
import com.example.cau_likelion_spring.recruitment.repository.OptionsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 백오피스 2단계 문항 작성 (현재 회차의 파트별) - 등록·수정·삭제·순서 변경.
 *
 * 저장 구조: InterviewQuestion(유형·필수 응답·순서) +
 *   장문 → InterviewScript(질문·설명·글자 수 제한)
 *   객관식 단일 → InterviewOption(질문·설명) + Options(선택지)
 *
 * 서류 접수 시작 후(또는 지원서가 접수된 파트)는 모든 변경을 막는다(409).
 * 그래서 수정 시 내용·선택지를 지우고 다시 만들어 id가 새로 발급돼도, 그것을 가리키는 답변이 없어 안전하다.
 * (지원자 답변은 문항 id FK와 선택지 id 문자열로 저장됨)
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestionAdminService {

    /** 백오피스에서 작성할 수 있는 문항 유형 */
    private static final Set<QuestionType> EDITABLE_TYPES = Set.of(QuestionType.LONG_TEXT, QuestionType.SINGLE_CHOICE);
    private static final int MIN_CHOICE_COUNT = 2;

    private final CurrentRoundFinder currentRoundFinder;
    private final ApplicationRepository applicationRepository;
    private final InterviewQuestionRepository interviewQuestionRepository;
    private final InterviewScriptRepository interviewScriptRepository;
    private final InterviewOptionRepository interviewOptionRepository;
    private final OptionsRepository optionsRepository;
    private final PartQuestionLoader partQuestionLoader;
    private final QuestionService questionService;

    /** 문항 등록 - 파트의 맨 뒤 순서로 붙는다 */
    @Transactional
    public QuestionResponse createQuestion(Long roundPartId, QuestionSaveRequest request) {
        Round round = currentRoundFinder.find();
        RoundPart roundPart = currentRoundFinder.findRoundPart(round, roundPartId);
        validateEditable(round, roundPart);
        validateRequest(request);

        InterviewQuestion question = interviewQuestionRepository.save(InterviewQuestion.builder()
                .round(round)
                .roundPart(roundPart)
                .type(request.type())
                .required(request.required())
                .sequence(interviewQuestionRepository.findMaxSequence(roundPart) + 1)
                .build());

        return saveContent(question, request);
    }

    /** 문항 수정 - 순서는 그대로 두고 유형·필수 여부·내용·선택지를 통째로 덮어쓴다 */
    @Transactional
    public QuestionResponse updateQuestion(Long questionId, QuestionSaveRequest request) {
        InterviewQuestion question = findEditableQuestion(questionId);
        validateRequest(request);

        question.update(request.type(), request.required());
        deleteContent(question);

        return saveContent(question, request);
    }

    /** 문항 삭제 - 남은 문항의 순서 값은 그대로 둔다 (정렬만 sequence 기준이라 빈 번호가 있어도 무방) */
    @Transactional
    public void deleteQuestion(Long questionId) {
        InterviewQuestion question = findEditableQuestion(questionId);
        deleteContent(question);
        interviewQuestionRepository.delete(question);
    }

    /**
     * 파트 문항의 노출 순서를 요청한 id 순서대로 1부터 다시 매긴다.
     * 일부만 보내면 순서가 꼬일 수 있어, 파트의 전체 문항 id가 중복 없이 모두 있어야 한다.
     */
    @Transactional
    public List<QuestionResponse> changeOrder(Long roundPartId, QuestionOrderRequest request) {
        Round round = currentRoundFinder.find();
        RoundPart roundPart = currentRoundFinder.findRoundPart(round, roundPartId);
        validateEditable(round, roundPart);

        List<Long> questionIds = request.questionIds();
        Map<Long, InterviewQuestion> questionById = interviewQuestionRepository.findAllByRoundPartOrderByIdAsc(roundPart)
                .stream()
                .collect(Collectors.toMap(InterviewQuestion::getId, Function.identity()));

        Set<Long> requestedIds = new HashSet<>(questionIds);
        if (requestedIds.size() != questionIds.size() || !requestedIds.equals(questionById.keySet())) {
            throw new CustomException(ErrorCode.RECRUITMENT_INVALID_QUESTION,
                    "파트의 전체 문항 id가 중복 없이 모두 포함되어야 합니다. 요청=" + questionIds
                            + ", 전체=" + questionById.keySet());
        }

        for (int i = 0; i < questionIds.size(); i++) {
            questionById.get(questionIds.get(i)).changeSequence(i + 1);
        }
        // 영속 상태(더티 체킹)라 별도 save() 불필요. 조회 쿼리 전에 자동 flush되어 바뀐 순서로 읽힌다.

        return partQuestionLoader.load(roundPart).stream()
                .map(questionService::toResponse)
                .toList();
    }

    /** 현재 회차 파트의 문항인지 확인하고, 수정 가능한 시점인지 검사한 뒤 반환한다 */
    private InterviewQuestion findEditableQuestion(Long questionId) {
        Round round = currentRoundFinder.find();
        InterviewQuestion question = interviewQuestionRepository.findById(questionId)
                .filter(found -> found.getRound() != null && round.getId().equals(found.getRound().getId()))
                .orElseThrow(() -> new CustomException(ErrorCode.RECRUITMENT_QUESTION_NOT_FOUND,
                        "현재 회차의 2단계 문항이 아닙니다. questionId=" + questionId));
        validateEditable(round, question.getRoundPart());
        return question;
    }

    /**
     * 서류 접수가 시작되면 문항을 바꿀 수 없다 (작성 중인 지원자가 옛 문항 id로 제출하게 되므로).
     * 접수 시작 후 일정을 뒤로 미뤄 다시 '시작 전'이 되더라도, 이미 접수된 지원서가 있으면 막는다.
     */
    private void validateEditable(Round round, RoundPart roundPart) {
        if (round.isApplyStarted(LocalDateTime.now())) {
            throw new CustomException(ErrorCode.RECRUITMENT_QUESTION_LOCKED,
                    "서류 접수가 시작된 후에는 문항을 수정할 수 없습니다. startDate=" + round.getStartDate());
        }
        if (applicationRepository.existsByRoundPart(roundPart)) {
            throw new CustomException(ErrorCode.RECRUITMENT_QUESTION_LOCKED,
                    "지원서가 접수된 파트의 문항은 수정할 수 없습니다. roundPartId=" + roundPart.getId());
        }
    }

    /** 유형별 규칙 검증 */
    private void validateRequest(QuestionSaveRequest request) {
        if (!EDITABLE_TYPES.contains(request.type())) {
            throw invalid("문항 유형은 LONG_TEXT(장문) 또는 SINGLE_CHOICE(객관식 단일)만 가능합니다.");
        }

        boolean hasOptions = request.options() != null && !request.options().isEmpty();
        if (request.type() == QuestionType.LONG_TEXT) {
            if (request.charLimit() == null) {
                throw invalid("장문 문항은 글자 수 제한(charLimit)을 입력해야 합니다.");
            }
            if (hasOptions) {
                throw invalid("장문 문항에는 선택지를 넣을 수 없습니다.");
            }
            return;
        }

        // SINGLE_CHOICE
        if (request.charLimit() != null) {
            throw invalid("객관식 문항에는 글자 수 제한을 넣을 수 없습니다.");
        }
        if (!hasOptions || request.options().size() < MIN_CHOICE_COUNT) {
            throw invalid("객관식 단일 문항은 선택지를 " + MIN_CHOICE_COUNT + "개 이상 입력해야 합니다.");
        }
    }

    /** 문항의 내용·선택지를 FK 참조 순서대로 삭제: 선택지 → 객관식 내용 → 장문 내용 */
    private void deleteContent(InterviewQuestion question) {
        optionsRepository.deleteAllByInterviewQuestion(question);
        interviewOptionRepository.deleteAllByInterviewQuestion(question);
        interviewScriptRepository.deleteAllByInterviewQuestion(question);
    }

    /** 유형에 맞는 내용(장문: Script / 객관식: Option + 선택지)을 저장하고 응답으로 변환한다 */
    private QuestionResponse saveContent(InterviewQuestion question, QuestionSaveRequest request) {
        Round round = question.getRound();
        RoundPart roundPart = question.getRoundPart();

        if (request.type() == QuestionType.LONG_TEXT) {
            InterviewScript script = interviewScriptRepository.save(InterviewScript.builder()
                    .interviewQuestion(question)
                    .round(round)
                    .roundPart(roundPart)
                    .question(request.question())
                    .detail(request.detail())
                    .limit(request.charLimit())
                    .build());
            return QuestionResponse.ofScript(question, script);
        }

        InterviewOption option = interviewOptionRepository.save(InterviewOption.builder()
                .interviewQuestion(question)
                .round(round)
                .roundPart(roundPart)
                .question(request.question())
                .detail(request.detail())
                .build());

        // 선택지는 만든 순(id)으로 노출되므로 요청 순서대로 저장한다
        List<Options> choices = optionsRepository.saveAll(request.options().stream()
                .map(label -> Options.builder()
                        .interviewOption(option)
                        .interviewQuestion(question)
                        .round(round)
                        .roundPart(roundPart)
                        .field(label)
                        .build())
                .toList());
        return QuestionResponse.ofOption(question, option, choices);
    }

    private CustomException invalid(String reason) {
        return new CustomException(ErrorCode.RECRUITMENT_INVALID_QUESTION, reason);
    }
}

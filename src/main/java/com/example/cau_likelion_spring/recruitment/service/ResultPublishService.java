package com.example.cau_likelion_spring.recruitment.service;

import com.example.cau_likelion_spring.global.exception.CustomException;
import com.example.cau_likelion_spring.global.exception.ErrorCode;
import com.example.cau_likelion_spring.recruitment.domain.ApplicationResult;
import com.example.cau_likelion_spring.recruitment.domain.ResultPublishStatus;
import com.example.cau_likelion_spring.recruitment.domain.ResultTemplateType;
import com.example.cau_likelion_spring.recruitment.domain.Round;
import com.example.cau_likelion_spring.recruitment.dto.DocumentPublishReserveRequest;
import com.example.cau_likelion_spring.recruitment.dto.ResultPublishCondition;
import com.example.cau_likelion_spring.recruitment.dto.ResultPublishResponse;
import com.example.cau_likelion_spring.recruitment.dto.ResultPublishResponse.Condition;
import com.example.cau_likelion_spring.recruitment.repository.ApplicationRepository;
import com.example.cau_likelion_spring.recruitment.repository.ResultRepository;
import com.example.cau_likelion_spring.recruitment.repository.ResultTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

/**
 * 백오피스 서류 결과 공개 예약·취소 (현재 회차 기준).
 *
 * 공개는 별도 처리 없이 "예약 시각이 지났는지"로 판정한다 (ResultPublishStatus) → 예약 시각이 되면 일괄 공개된다.
 * 보류(HOLD)자는 공개 대상에서 제외되며, 지원자 결과 조회 API에서 "평가 중"으로 보여준다.
 * 예약 중(RESERVED)에는 서류 결과·면접 일정·서류 문구 입력이 잠긴다 (ResultAdminService, ResultTemplateService).
 * TODO: 최종 결과 공개 예약(결과 확인 종료일·모집 종료일 포함)은 면접 전형 기능과 함께 추가
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResultPublishService {

    private final CurrentRoundFinder currentRoundFinder;
    private final ApplicationRepository applicationRepository;
    private final ResultRepository resultRepository;
    private final ResultTemplateRepository resultTemplateRepository;

    public ResultPublishResponse getDocumentPublishStatus() {
        return buildResponse(currentRoundFinder.find(), LocalDateTime.now());
    }

    @Transactional
    public ResultPublishResponse reserveDocument(DocumentPublishReserveRequest request) {
        Round round = currentRoundFinder.find();
        LocalDateTime now = LocalDateTime.now();

        if (round.getDocumentPublishStatus(now) != ResultPublishStatus.INPUTTING) {
            throw new CustomException(ErrorCode.RECRUITMENT_PUBLISH_STATE_CONFLICT,
                    "이미 서류 결과가 예약 또는 공개되었습니다. 예약을 변경하려면 취소 후 다시 예약해주세요.");
        }
        if (!request.publishAt().isAfter(now)) {
            throw new CustomException(ErrorCode.RECRUITMENT_INVALID_PUBLISH_SCHEDULE,
                    "과거 시각으로는 예약할 수 없습니다. publishAt=" + request.publishAt());
        }
        validateConditions(documentConditions(round));

        round.reserveDocumentResult(request.publishAt()); // 영속 상태라 더티 체킹으로 반영
        return buildResponse(round, now);
    }

    @Transactional
    public ResultPublishResponse cancelDocument() {
        Round round = currentRoundFinder.find();
        LocalDateTime now = LocalDateTime.now();

        ResultPublishStatus status = round.getDocumentPublishStatus(now);
        if (status == ResultPublishStatus.INPUTTING) {
            throw new CustomException(ErrorCode.RECRUITMENT_PUBLISH_STATE_CONFLICT, "서류 결과 공개 예약이 없습니다.");
        }
        if (status == ResultPublishStatus.PUBLISHED) {
            throw new CustomException(ErrorCode.RECRUITMENT_PUBLISH_STATE_CONFLICT,
                    "서류 결과가 이미 공개되어 예약을 취소할 수 없습니다. 공개 후에는 결과 입력 화면에서 바로 수정해주세요.");
        }

        round.cancelDocumentResultReservation();
        return buildResponse(round, now);
    }

    private List<Condition> documentConditions(Round round) {
        long total = applicationRepository.countByRoundPart_Round(round);
        long entered = resultRepository.countByApplication_RoundPart_RoundAndDocumentResultIsNotNull(round);
        long passed = resultRepository.countByApplication_RoundPart_RoundAndDocumentResult(round, ApplicationResult.PASS);
        long scheduleMissing = resultRepository
                .countByApplication_RoundPart_RoundAndDocumentResultAndFixedInterviewDateIsNull(round, ApplicationResult.PASS);

        return List.of(
                condition(ResultPublishCondition.DOCUMENT_RESULT_ENTERED, entered == total,
                        total + "명 중 " + entered + "명 입력"),
                condition(ResultPublishCondition.INTERVIEW_SCHEDULE_ENTERED, scheduleMissing == 0,
                        "서류 합격자 " + passed + "명 중 " + (passed - scheduleMissing) + "명 입력"),
                templateCondition(round, ResultTemplateType.DOCUMENT_PASS, ResultTemplateType.DOCUMENT_FAIL));
    }

    /** 문구는 저장한 적이 있어야 작성한 것으로 본다 (기본 문구 그대로는 미작성) */
    private Condition templateCondition(Round round, ResultTemplateType... templateTypes) {
        List<String> missing = Stream.of(templateTypes)
                .filter(templateType -> !resultTemplateRepository.existsByRoundAndType(round, templateType))
                .map(ResultTemplateType::getLabel)
                .toList();
        String detail = missing.isEmpty() ? "작성 완료" : "미작성: " + String.join(", ", missing);
        return condition(ResultPublishCondition.DOCUMENT_TEMPLATES_WRITTEN, missing.isEmpty(), detail);
    }

    private Condition condition(ResultPublishCondition type, boolean satisfied, String detail) {
        return new Condition(type, type.getLabel(), satisfied, detail);
    }

    private void validateConditions(List<Condition> conditions) {
        List<String> unmet = conditions.stream()
                .filter(condition -> !condition.satisfied())
                .map(condition -> condition.label() + "(" + condition.detail() + ")")
                .toList();
        if (!unmet.isEmpty()) {
            throw new CustomException(ErrorCode.RECRUITMENT_PUBLISH_CONDITION_NOT_MET,
                    "서류 결과 공개 조건을 만족하지 않습니다: " + String.join(", ", unmet));
        }
    }

    private ResultPublishResponse buildResponse(Round round, LocalDateTime now) {
        ResultPublishStatus status = round.getDocumentPublishStatus(now);
        List<Condition> conditions = documentConditions(round);
        return new ResultPublishResponse(status, round.getDocumentResultPublishAt(),
                status == ResultPublishStatus.INPUTTING && conditions.stream().allMatch(Condition::satisfied),
                status == ResultPublishStatus.RESERVED,
                conditions);
    }
}

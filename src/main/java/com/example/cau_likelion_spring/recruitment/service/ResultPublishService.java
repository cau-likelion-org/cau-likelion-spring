package com.example.cau_likelion_spring.recruitment.service;

import com.example.cau_likelion_spring.global.exception.CustomException;
import com.example.cau_likelion_spring.global.exception.ErrorCode;
import com.example.cau_likelion_spring.recruitment.domain.ApplicationResult;
import com.example.cau_likelion_spring.recruitment.domain.ResultPublishStatus;
import com.example.cau_likelion_spring.recruitment.domain.ResultTemplateType;
import com.example.cau_likelion_spring.recruitment.domain.Round;
import com.example.cau_likelion_spring.recruitment.dto.DocumentPublishReserveRequest;
import com.example.cau_likelion_spring.recruitment.dto.FinalPublishReserveRequest;
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
 * 백오피스 전형별(서류·최종) 결과 공개 예약·취소 (현재 회차 기준).
 *
 * 공개는 별도 처리 없이 "예약 시각이 지났는지"로 판정한다 (ResultPublishStatus) → 예약 시각이 되면 일괄 공개된다.
 * 보류(HOLD)자는 공개 대상에서 제외되며, 지원자 결과 조회 API에서 "평가 중"으로 보여준다.
 * 예약 중(RESERVED)에는 해당 전형의 결과·면접 일정·발표 문구 입력이 잠긴다 (ResultAdminService, ResultTemplateService).
 * 최종 결과는 서류 결과 공개 예약 이후에만 예약할 수 있고, 서류 결과 예약은 최종 결과 예약이 없을 때만 취소할 수 있다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResultPublishService {

    /** 불합격자 보관 기간 (모집 종료일로부터). 결과 확인 종료일은 이 기간을 넘을 수 없다 */
    private static final int REJECTED_RETENTION_MONTHS = 3;

    private final CurrentRoundFinder currentRoundFinder;
    private final ApplicationRepository applicationRepository;
    private final ResultRepository resultRepository;
    private final ResultTemplateRepository resultTemplateRepository;

    // ===== 서류 결과 =====

    public ResultPublishResponse getDocumentPublishStatus() {
        return buildDocumentResponse(currentRoundFinder.find(), LocalDateTime.now());
    }

    @Transactional
    public ResultPublishResponse reserveDocument(DocumentPublishReserveRequest request) {
        Round round = currentRoundFinder.find();
        LocalDateTime now = LocalDateTime.now();

        validateInputting("서류", round.getDocumentPublishStatus(now));
        validateFuture(request.publishAt(), now);
        validateConditions("서류", documentConditions(round));

        round.reserveDocumentResult(request.publishAt()); // 영속 상태라 더티 체킹으로 반영
        return buildDocumentResponse(round, now);
    }

    @Transactional
    public ResultPublishResponse cancelDocument() {
        Round round = currentRoundFinder.find();
        LocalDateTime now = LocalDateTime.now();

        validateCancellable("서류", round.getDocumentPublishStatus(now));
        if (round.getFinalPublishStatus(now) != ResultPublishStatus.INPUTTING) {
            throw new CustomException(ErrorCode.RECRUITMENT_PUBLISH_STATE_CONFLICT,
                    "최종 결과가 예약되어 있어 서류 결과 예약을 취소할 수 없습니다. 최종 결과 예약을 먼저 취소해주세요.");
        }

        round.cancelDocumentResultReservation();
        return buildDocumentResponse(round, now);
    }

    // ===== 최종 결과 =====

    public ResultPublishResponse getFinalPublishStatus() {
        return buildFinalResponse(currentRoundFinder.find(), LocalDateTime.now());
    }

    @Transactional
    public ResultPublishResponse reserveFinal(FinalPublishReserveRequest request) {
        Round round = currentRoundFinder.find();
        LocalDateTime now = LocalDateTime.now();

        validateInputting("최종", round.getFinalPublishStatus(now));
        validateFuture(request.publishAt(), now);
        validateConditions("최종", finalConditions(round, now));
        validateFinalSchedule(round, request);

        round.reserveFinalResult(request.publishAt(), request.resultCheckDeadLine());
        return buildFinalResponse(round, now);
    }

    @Transactional
    public ResultPublishResponse cancelFinal() {
        Round round = currentRoundFinder.find();
        LocalDateTime now = LocalDateTime.now();

        validateCancellable("최종", round.getFinalPublishStatus(now));

        round.cancelFinalResultReservation();
        return buildFinalResponse(round, now);
    }

    // ===== 조건 =====

    /**
     * 서류 결과 공개 조건 (각각 달성 수 / 전체 수).
     * (1) 서류 결과 입력된 지원자 수 / 전체 지원자 수
     * (2) 서류 합격자 중 면접 일시·장소 입력된 수 / 서류 합격자 수
     * (3) 저장한 서류 문구 수 / 2 (합격·불합격)
     */
    private List<Condition> documentConditions(Round round) {
        long applicants = applicationRepository.countByRoundPart_Round(round);
        long resultEntered = resultRepository.countByApplication_RoundPart_RoundAndDocumentResultIsNotNull(round);
        long passed = countDocumentPassed(round);
        long scheduleEntered = resultRepository
                .countByApplication_RoundPart_RoundAndDocumentResultAndFixedInterviewDateIsNotNullAndFixedInterviewSpotIsNotNull(
                        round, ApplicationResult.PASS);

        return List.of(
                condition(ResultPublishCondition.DOCUMENT_RESULT_ENTERED, resultEntered, applicants,
                        applicants + "명 중 " + resultEntered + "명 입력"),
                condition(ResultPublishCondition.INTERVIEW_SCHEDULE_ENTERED, scheduleEntered, passed,
                        "서류 합격자 " + passed + "명 중 " + scheduleEntered + "명 입력"),
                templateCondition(ResultPublishCondition.DOCUMENT_TEMPLATES_WRITTEN, round,
                        ResultTemplateType.DOCUMENT_PASS, ResultTemplateType.DOCUMENT_FAIL));
    }

    /**
     * 최종 결과 공개 조건 (각각 달성 수 / 전체 수).
     * 서류 결과 공개 예약 여부는 1 / 1(완료) 또는 0 / 1(미완료)로 표기한다.
     */
    private List<Condition> finalConditions(Round round, LocalDateTime now) {
        ResultPublishStatus documentStatus = round.getDocumentPublishStatus(now);
        long passed = countDocumentPassed(round);
        long finalEntered = resultRepository
                .countByApplication_RoundPart_RoundAndDocumentResultAndFinalResultIsNotNull(round, ApplicationResult.PASS);

        return List.of(
                condition(ResultPublishCondition.DOCUMENT_PUBLISH_RESERVED,
                        documentStatus != ResultPublishStatus.INPUTTING ? 1 : 0, 1,
                        "서류 결과 " + documentStatus.getLabel()),
                condition(ResultPublishCondition.FINAL_RESULT_ENTERED, finalEntered, passed,
                        "서류 합격자 " + passed + "명 중 " + finalEntered + "명 입력"),
                templateCondition(ResultPublishCondition.FINAL_TEMPLATES_WRITTEN, round,
                        ResultTemplateType.FINAL_PASS, ResultTemplateType.FINAL_FAIL));
    }

    private long countDocumentPassed(Round round) {
        return resultRepository.countByApplication_RoundPart_RoundAndDocumentResult(round, ApplicationResult.PASS);
    }

    /** 문구는 저장한 적이 있어야 작성한 것으로 본다 (기본 문구 그대로는 미작성) */
    private Condition templateCondition(ResultPublishCondition type, Round round, ResultTemplateType... templateTypes) {
        List<String> missing = Stream.of(templateTypes)
                .filter(templateType -> !resultTemplateRepository.existsByRoundAndType(round, templateType))
                .map(ResultTemplateType::getLabel)
                .toList();
        String detail = missing.isEmpty() ? "작성 완료" : "미작성: " + String.join(", ", missing);
        return condition(type, templateTypes.length - missing.size(), templateTypes.length, detail);
    }

    private Condition condition(ResultPublishCondition type, long achieved, long total, String detail) {
        return new Condition(type, type.getLabel(), achieved == total, achieved, total, detail);
    }

    private void validateConditions(String stageName, List<Condition> conditions) {
        List<String> unmet = conditions.stream()
                .filter(condition -> !condition.satisfied())
                .map(condition -> condition.label() + "(" + condition.detail() + ")")
                .toList();
        if (!unmet.isEmpty()) {
            throw new CustomException(ErrorCode.RECRUITMENT_PUBLISH_CONDITION_NOT_MET,
                    stageName + " 결과 공개 조건을 만족하지 않습니다: " + String.join(", ", unmet));
        }
    }

    // ===== 일정·상태 검증 =====

    private void validateInputting(String stageName, ResultPublishStatus status) {
        if (status != ResultPublishStatus.INPUTTING) {
            throw new CustomException(ErrorCode.RECRUITMENT_PUBLISH_STATE_CONFLICT,
                    "이미 " + stageName + " 결과가 예약 또는 공개되었습니다. 예약을 변경하려면 취소 후 다시 예약해주세요.");
        }
    }

    private void validateFuture(LocalDateTime publishAt, LocalDateTime now) {
        if (!publishAt.isAfter(now)) {
            throw new CustomException(ErrorCode.RECRUITMENT_INVALID_PUBLISH_SCHEDULE,
                    "과거 시각으로는 예약할 수 없습니다. publishAt=" + publishAt);
        }
    }

    /**
     * 서류 공개 < 최종 공개 < 결과 확인 종료 ≤ 모집 종료일 + 3개월.
     * 모집 종료일은 우선 모집공고의 서류 모집 마감일(deadLine)로 본다.
     */
    private void validateFinalSchedule(Round round, FinalPublishReserveRequest request) {
        if (!request.publishAt().isAfter(round.getDocumentResultPublishAt())) {
            throw new CustomException(ErrorCode.RECRUITMENT_INVALID_PUBLISH_SCHEDULE,
                    "최종 결과 공개 일시는 서류 결과 공개 일시(" + round.getDocumentResultPublishAt() + ") 이후여야 합니다.");
        }
        if (!request.resultCheckDeadLine().isAfter(request.publishAt())) {
            throw new CustomException(ErrorCode.RECRUITMENT_INVALID_PUBLISH_SCHEDULE,
                    "결과 확인 종료일은 최종 결과 공개 일시 이후여야 합니다.");
        }
        if (round.getDeadLine() == null) {
            throw new CustomException(ErrorCode.RECRUITMENT_INVALID_PUBLISH_SCHEDULE,
                    "모집공고의 서류 모집 마감일이 없어 결과 확인 기간을 검증할 수 없습니다. 모집공고 일정을 먼저 등록해주세요.");
        }
        LocalDateTime retentionEnd = round.getDeadLine().plusMonths(REJECTED_RETENTION_MONTHS);
        if (request.resultCheckDeadLine().isAfter(retentionEnd)) {
            throw new CustomException(ErrorCode.RECRUITMENT_INVALID_PUBLISH_SCHEDULE,
                    "결과 확인 종료일은 불합격자 보관 기간(서류 모집 마감일로부터 " + REJECTED_RETENTION_MONTHS
                            + "개월, " + retentionEnd + ")을 넘을 수 없습니다.");
        }
    }

    private void validateCancellable(String stageName, ResultPublishStatus status) {
        if (status == ResultPublishStatus.INPUTTING) {
            throw new CustomException(ErrorCode.RECRUITMENT_PUBLISH_STATE_CONFLICT,
                    stageName + " 결과 공개 예약이 없습니다.");
        }
        if (status == ResultPublishStatus.PUBLISHED) {
            throw new CustomException(ErrorCode.RECRUITMENT_PUBLISH_STATE_CONFLICT,
                    stageName + " 결과가 이미 공개되어 예약을 취소할 수 없습니다. 공개 후에는 결과 입력 화면에서 바로 수정해주세요.");
        }
    }

    // ===== 응답 =====

    private ResultPublishResponse buildDocumentResponse(Round round, LocalDateTime now) {
        ResultPublishStatus status = round.getDocumentPublishStatus(now);
        List<Condition> conditions = documentConditions(round);
        return new ResultPublishResponse(status, round.getDocumentResultPublishAt(), null,
                status == ResultPublishStatus.INPUTTING && allSatisfied(conditions),
                status == ResultPublishStatus.RESERVED && round.getFinalPublishStatus(now) == ResultPublishStatus.INPUTTING,
                conditions);
    }

    private ResultPublishResponse buildFinalResponse(Round round, LocalDateTime now) {
        ResultPublishStatus status = round.getFinalPublishStatus(now);
        List<Condition> conditions = finalConditions(round, now);
        return new ResultPublishResponse(status, round.getFinalResultDate(), round.getResultCheckDeadLine(),
                status == ResultPublishStatus.INPUTTING && allSatisfied(conditions),
                status == ResultPublishStatus.RESERVED,
                conditions);
    }

    private boolean allSatisfied(List<Condition> conditions) {
        return conditions.stream().allMatch(Condition::satisfied);
    }
}

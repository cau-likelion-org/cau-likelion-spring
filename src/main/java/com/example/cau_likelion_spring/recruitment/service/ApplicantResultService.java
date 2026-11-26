package com.example.cau_likelion_spring.recruitment.service;

import com.example.cau_likelion_spring.global.exception.CustomException;
import com.example.cau_likelion_spring.global.exception.ErrorCode;
import com.example.cau_likelion_spring.recruitment.domain.Applicant;
import com.example.cau_likelion_spring.recruitment.domain.Application;
import com.example.cau_likelion_spring.recruitment.domain.ApplicationResult;
import com.example.cau_likelion_spring.recruitment.domain.Result;
import com.example.cau_likelion_spring.recruitment.domain.ResultPublishStatus;
import com.example.cau_likelion_spring.recruitment.domain.ResultTemplate;
import com.example.cau_likelion_spring.recruitment.domain.ResultTemplateType;
import com.example.cau_likelion_spring.recruitment.domain.Round;
import com.example.cau_likelion_spring.recruitment.dto.ApplicantResultResponse;
import com.example.cau_likelion_spring.recruitment.dto.ApplicantResultResponse.Stage;
import com.example.cau_likelion_spring.recruitment.dto.ApplicantResultResponse.Status;
import com.example.cau_likelion_spring.recruitment.repository.ApplicantRepository;
import com.example.cau_likelion_spring.recruitment.repository.ApplicationRepository;
import com.example.cau_likelion_spring.recruitment.repository.ResultRepository;
import com.example.cau_likelion_spring.recruitment.repository.ResultTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 지원자 본인의 서류·최종 결과 확인 (7.5.1).
 *
 * 판정 순서
 * 1. 이번 회차에 제출한 지원서가 없으면 NOT_SUBMITTED
 * 2. 결과 확인 종료일이 지났으면 CHECK_PERIOD_ENDED (종료일은 최종 결과 예약 때 정해지고, 그 전에는 판정하지 않는다)
 * 3. 서류: 공개 전이거나 보류·미입력이면 EVALUATING, 불합격이면 서류 불합격 문구 (최종 조회여도 여기서 끝)
 * 4. 최종: 공개 전이거나 보류·미입력이면 EVALUATING, 합격·불합격이면 최종 문구
 *
 * 조회할 때마다 DB에서 계산하므로 공개 후 결과·면접 정보·문구를 수정하면 바로 반영된다.
 * 문구는 백오피스 미리보기와 같은 ResultMessageRenderer로 치환한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApplicantResultService {

    private final ApplicantRepository applicantRepository;
    private final ApplicationRepository applicationRepository;
    private final ResultRepository resultRepository;
    private final ResultTemplateRepository resultTemplateRepository;
    private final CurrentRoundFinder currentRoundFinder;
    private final ResultMessageRenderer resultMessageRenderer;

    /** 서류 결과 조회 */
    public ApplicantResultResponse getDocumentResult(Long applicantId) {
        return getResult(applicantId, Stage.DOCUMENT);
    }

    /** 최종 결과 조회 (서류 불합격자는 서류 불합격 결과를 그대로 돌려준다) */
    public ApplicantResultResponse getFinalResult(Long applicantId) {
        return getResult(applicantId, Stage.FINAL);
    }

    private ApplicantResultResponse getResult(Long applicantId, Stage requestedStage) {
        Applicant applicant = applicantRepository.findById(applicantId)
                .orElseThrow(() -> new CustomException(ErrorCode.RECRUITMENT_APPLICANT_NOT_FOUND));
        Round round = currentRoundFinder.find();
        LocalDateTime now = LocalDateTime.now();

        Optional<Application> application = applicationRepository.findByApplicantAndRoundPart_Round(applicant, round);
        if (application.isEmpty()) {
            return ApplicantResultResponse.fixed(requestedStage, round, Status.NOT_SUBMITTED);
        }
        if (round.getResultCheckDeadLine() != null && now.isAfter(round.getResultCheckDeadLine())) {
            return ApplicantResultResponse.fixed(requestedStage, round, Status.CHECK_PERIOD_ENDED);
        }

        Result result = resultRepository.findByApplication(application.get()).orElse(null);

        // 서류 결과
        if (round.getDocumentPublishStatus(now) != ResultPublishStatus.PUBLISHED
                || result == null || !isDecided(result.getDocumentResult())) {
            return ApplicantResultResponse.fixed(Stage.DOCUMENT, round, Status.EVALUATING);
        }
        if (result.getDocumentResult() == ApplicationResult.FAIL) {
            return rendered(Stage.DOCUMENT, Status.FAIL, ResultTemplateType.DOCUMENT_FAIL, round, applicant, result);
        }
        if (requestedStage == Stage.DOCUMENT) {
            return rendered(Stage.DOCUMENT, Status.PASS, ResultTemplateType.DOCUMENT_PASS, round, applicant, result);
        }

        // 최종 결과 (서류 합격자)
        if (round.getFinalPublishStatus(now) != ResultPublishStatus.PUBLISHED || !isDecided(result.getFinalResult())) {
            return ApplicantResultResponse.fixed(Stage.FINAL, round, Status.EVALUATING);
        }
        return result.getFinalResult() == ApplicationResult.PASS
                ? rendered(Stage.FINAL, Status.PASS, ResultTemplateType.FINAL_PASS, round, applicant, result)
                : rendered(Stage.FINAL, Status.FAIL, ResultTemplateType.FINAL_FAIL, round, applicant, result);
    }

    /** 합격·불합격으로 정해졌는지 (미입력·보류는 지원자에게 공개하지 않는다) */
    private boolean isDecided(ApplicationResult applicationResult) {
        return applicationResult == ApplicationResult.PASS || applicationResult == ApplicationResult.FAIL;
    }

    /** 회차의 결과 안내 문구(저장 전이면 기본 문구)를 지원자 값으로 치환한다 */
    private ApplicantResultResponse rendered(Stage stage, Status status, ResultTemplateType type,
                                             Round round, Applicant applicant, Result result) {
        String text = resultTemplateRepository.findByRoundAndType(round, type)
                .map(ResultTemplate::getText)
                .orElse(type.getDefaultText());
        return ApplicantResultResponse.of(stage, round, status, resultMessageRenderer.render(text, applicant, result));
    }
}

package com.example.cau_likelion_spring.recruitment.service;

import com.example.cau_likelion_spring.global.exception.CustomException;
import com.example.cau_likelion_spring.global.exception.ErrorCode;
import com.example.cau_likelion_spring.recruitment.domain.Application;
import com.example.cau_likelion_spring.recruitment.domain.Result;
import com.example.cau_likelion_spring.recruitment.domain.ResultPublishStatus;
import com.example.cau_likelion_spring.recruitment.domain.Round;
import com.example.cau_likelion_spring.recruitment.dto.DocumentResultRequest;
import com.example.cau_likelion_spring.recruitment.dto.DocumentResultResponse;
import com.example.cau_likelion_spring.recruitment.dto.InterviewScheduleRequest;
import com.example.cau_likelion_spring.recruitment.dto.InterviewScheduleResponse;
import com.example.cau_likelion_spring.recruitment.dto.InterviewScheduleResponse.Warning;
import com.example.cau_likelion_spring.recruitment.dto.InterviewScheduleResponse.WarningType;
import com.example.cau_likelion_spring.recruitment.repository.ApplicantInterviewTimeRepository;
import com.example.cau_likelion_spring.recruitment.repository.ApplicationRepository;
import com.example.cau_likelion_spring.recruitment.repository.ResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 백오피스 서류 결과·확정 면접 일정 입력 (현재 회차 제출자 한정).
 *
 * 결과(Result)는 지원서 제출 시 만들지 않으므로, 처음 입력할 때 만들고 이후에는 덮어쓴다 (이력 미보관).
 * 서류 결과 공개 예약 중(RESERVED)에는 서류 결과·면접 일정 입력이 잠긴다 (예약 취소 후 수정).
 * TODO: 공개 후 변경 규칙(보류로 변경 불가, 합격 변경 시 면접 정보 필수 등)은 공개 후 수정 기능(7.8.1·7.8.2)과 함께 추가
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResultAdminService {

    /** 면접 가능 시간 슬롯 하나의 길이 (지원자는 10:00~10:15처럼 15분 단위로 고른다) */
    private static final Duration INTERVIEW_SLOT_LENGTH = Duration.ofMinutes(15);

    private final CurrentRoundFinder currentRoundFinder;
    private final ApplicationRepository applicationRepository;
    private final ResultRepository resultRepository;
    private final ApplicantInterviewTimeRepository applicantInterviewTimeRepository;

    /** 요청한 지원서들의 서류 결과를 한 번에 저장한다. 하나라도 잘못된 id가 있으면 아무것도 저장하지 않는다 */
    @Transactional
    public List<DocumentResultResponse> updateDocumentResults(DocumentResultRequest request) {
        List<Long> applicationIds = request.applicationIds();
        Set<Long> distinctIds = new HashSet<>(applicationIds);
        if (distinctIds.size() != applicationIds.size()) {
            throw new CustomException(ErrorCode.RECRUITMENT_DUPLICATE_APPLICATION_ID,
                    "같은 지원서가 중복으로 요청되었습니다. applicationIds=" + applicationIds);
        }

        Round round = currentRoundFinder.find();
        validateNotReserved(round);
        List<Application> applications = applicationRepository.findAllByIdInAndRoundPart_Round(distinctIds, round);
        if (applications.size() != distinctIds.size()) {
            Set<Long> foundIds = applications.stream().map(Application::getId).collect(Collectors.toSet());
            List<Long> missingIds = applicationIds.stream().filter(id -> !foundIds.contains(id)).toList();
            throw new CustomException(ErrorCode.RECRUITMENT_APPLICATION_NOT_FOUND,
                    "현재 회차에 제출된 지원서가 아닙니다. applicationIds=" + missingIds);
        }

        Map<Long, Result> resultByApplicationId = resultRepository.findAllByApplicationIn(applications).stream()
                .collect(Collectors.toMap(result -> result.getApplication().getId(),
                        Function.identity(), (first, second) -> first, HashMap::new)); // 새로 만든 결과도 넣으므로 변경 가능한 맵

        List<Result> newResults = new ArrayList<>();
        for (Application application : applications) {
            Result result = resultByApplicationId.get(application.getId());
            if (result == null) {
                Result created = Result.builder()
                        .application(application)
                        .applicant(application.getApplicant())
                        .documentResult(request.result())
                        .build();
                newResults.add(created);
                resultByApplicationId.put(application.getId(), created);
            } else {
                result.changeDocumentResult(request.result()); // 영속 상태라 더티 체킹으로 반영
            }
        }
        resultRepository.saveAll(newResults);

        // 요청한 순서대로 응답
        return applicationIds.stream()
                .map(resultByApplicationId::get)
                .map(DocumentResultResponse::from)
                .toList();
    }

    /**
     * 지원자 한 명의 확정 면접 일시·장소를 저장한다 (덮어쓰기).
     * 서류 합격자만 입력할 수 있다. 장소는 비워 보내면 지워진다.
     * 지원자 가능 시간 밖이거나 같은 일시 중복 배정이어도 저장하고, 응답의 warnings로 알린다.
     */
    @Transactional
    public InterviewScheduleResponse updateInterviewSchedule(Long applicationId, InterviewScheduleRequest request) {
        Round round = currentRoundFinder.find();
        validateNotReserved(round);
        Application application = applicationRepository.findByIdAndRoundPart_Round(applicationId, round)
                .orElseThrow(() -> new CustomException(ErrorCode.RECRUITMENT_APPLICATION_NOT_FOUND,
                        "현재 회차에 제출된 지원서가 아닙니다. applicationId=" + applicationId));

        Result result = resultRepository.findByApplication(application)
                .filter(Result::isDocumentPassed)
                .orElseThrow(() -> new CustomException(ErrorCode.RECRUITMENT_NOT_DOCUMENT_PASSED,
                        "서류 합격자만 면접 일정을 입력할 수 있습니다. applicationId=" + applicationId));

        String spot = StringUtils.hasText(request.fixedInterviewSpot()) ? request.fixedInterviewSpot().trim() : null;
        result.changeInterviewSchedule(request.fixedInterviewDate(), spot); // 영속 상태라 더티 체킹으로 반영

        return InterviewScheduleResponse.of(result, checkScheduleWarnings(round, application, request.fixedInterviewDate()));
    }

    /** 서류 결과 공개 예약 중에는 입력할 수 없다 */
    private void validateNotReserved(Round round) {
        if (round.getDocumentPublishStatus(LocalDateTime.now()) == ResultPublishStatus.RESERVED) {
            throw new CustomException(ErrorCode.RECRUITMENT_RESULT_LOCKED,
                    "서류 결과 공개 예약 중에는 수정할 수 없습니다. 예약을 취소한 후 수정해주세요.");
        }
    }

    /**
     * 확정 면접 일정 경고 (저장은 막지 않는다).
     * - 지원자가 고른 면접 가능 시간 슬롯([시작, 시작 + 15분)) 중 어디에도 속하지 않는 일시
     * - 현재 회차에서 같은 일시에 다른 지원자가 이미 배정됨
     */
    private List<Warning> checkScheduleWarnings(Round round, Application application, LocalDateTime fixedInterviewDate) {
        List<Warning> warnings = new ArrayList<>();

        boolean inAvailableTime = applicantInterviewTimeRepository
                .findAllByApplicantAndInterviewTime_Round(application.getApplicant(), round).stream()
                .map(selected -> selected.getInterviewTime().getInterviewDate())
                .anyMatch(slotStart -> !fixedInterviewDate.isBefore(slotStart)
                        && fixedInterviewDate.isBefore(slotStart.plus(INTERVIEW_SLOT_LENGTH)));
        if (!inAvailableTime) {
            warnings.add(new Warning(WarningType.OUT_OF_AVAILABLE_TIME, "지원자가 선택한 면접 가능 시간이 아닙니다."));
        }

        long sameTimeCount = resultRepository.countByFixedInterviewDateAndApplication_RoundPart_RoundAndApplicationNot(
                fixedInterviewDate, round, application);
        if (sameTimeCount > 0) {
            warnings.add(new Warning(WarningType.DUPLICATE_SCHEDULE,
                    "같은 일시에 이미 배정된 지원자가 " + sameTimeCount + "명 있습니다."));
        }
        return warnings;
    }
}

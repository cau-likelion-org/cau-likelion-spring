package com.example.cau_likelion_spring.recruitment.service;

import com.example.cau_likelion_spring.global.exception.CustomException;
import com.example.cau_likelion_spring.global.exception.ErrorCode;
import com.example.cau_likelion_spring.recruitment.domain.Application;
import com.example.cau_likelion_spring.recruitment.domain.Result;
import com.example.cau_likelion_spring.recruitment.domain.Round;
import com.example.cau_likelion_spring.recruitment.dto.DocumentResultRequest;
import com.example.cau_likelion_spring.recruitment.dto.DocumentResultResponse;
import com.example.cau_likelion_spring.recruitment.repository.ApplicationRepository;
import com.example.cau_likelion_spring.recruitment.repository.ResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 백오피스 서류 결과 입력 (현재 회차 제출자 한정).
 *
 * 결과(Result)는 지원서 제출 시 만들지 않으므로, 처음 입력할 때 만들고 이후에는 덮어쓴다 (이력 미보관).
 * TODO: 공개 예약 상태 잠금, 공개 후 변경 규칙(보류로 변경 불가, 합격 변경 시 면접 정보 필수 등)은 공개 예약 기능과 함께 추가
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResultAdminService {

    private final CurrentRoundFinder currentRoundFinder;
    private final ApplicationRepository applicationRepository;
    private final ResultRepository resultRepository;

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
}

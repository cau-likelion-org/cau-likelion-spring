package com.example.cau_likelion_spring.recruitment.service;

import com.example.cau_likelion_spring.global.exception.CustomException;
import com.example.cau_likelion_spring.global.exception.ErrorCode;
import com.example.cau_likelion_spring.recruitment.domain.Application;
import com.example.cau_likelion_spring.recruitment.domain.ResultTemplate;
import com.example.cau_likelion_spring.recruitment.domain.ResultTemplateType;
import com.example.cau_likelion_spring.recruitment.domain.ResultTemplateVariable;
import com.example.cau_likelion_spring.recruitment.domain.Round;
import com.example.cau_likelion_spring.recruitment.dto.ResultTemplatePreviewRequest;
import com.example.cau_likelion_spring.recruitment.dto.ResultTemplatePreviewResponse;
import com.example.cau_likelion_spring.recruitment.dto.ResultTemplateResponse;
import com.example.cau_likelion_spring.recruitment.dto.ResultTemplateSaveRequest;
import com.example.cau_likelion_spring.recruitment.repository.ApplicationRepository;
import com.example.cau_likelion_spring.recruitment.repository.ResultRepository;
import com.example.cau_likelion_spring.recruitment.repository.ResultTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 백오피스 결과 안내 문구 템플릿 조회·저장·미리보기 (현재 회차 기준).
 * 해당 전형(서류·최종)의 결과 공개 예약 중(RESERVED)에는 문구를 수정할 수 없다 (예약 취소 후 수정).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResultTemplateService {

    private final CurrentRoundFinder currentRoundFinder;
    private final ResultTemplateRepository resultTemplateRepository;
    private final ApplicationRepository applicationRepository;
    private final ResultRepository resultRepository;
    private final ResultMessageRenderer resultMessageRenderer;

    /** 템플릿 하나를 반환한다. 저장 전이면 기본 문구로 채운다 */
    public ResultTemplateResponse getTemplate(ResultTemplateType type) {
        Round round = currentRoundFinder.find();
        return resultTemplateRepository.findByRoundAndType(round, type)
                .map(saved -> ResultTemplateResponse.of(type, saved.getText(), false))
                .orElseGet(() -> ResultTemplateResponse.of(type, type.getDefaultText(), true));
    }

    /** 템플릿 하나를 저장한다 (처음이면 만들고, 이후에는 덮어쓰기) */
    @Transactional
    public ResultTemplateResponse saveTemplate(ResultTemplateType type, ResultTemplateSaveRequest request) {
        validateVariables(type, request.text());
        Round round = currentRoundFinder.find();
        validateNotReserved(round, type);

        resultTemplateRepository.findByRoundAndType(round, type)
                .ifPresentOrElse(
                        template -> template.changeText(request.text()), // 영속 상태라 더티 체킹으로 반영
                        () -> resultTemplateRepository.save(ResultTemplate.builder()
                                .round(round)
                                .type(type)
                                .text(request.text())
                                .build()));

        return ResultTemplateResponse.of(type, request.text(), false);
    }

    /** 편집 중인 문구를 지원자 한 명의 실제 값으로 바꿔서 보여준다 (저장하지 않음) */
    public ResultTemplatePreviewResponse preview(ResultTemplateType type, ResultTemplatePreviewRequest request) {
        validateVariables(type, request.text());
        Round round = currentRoundFinder.find();

        Application application = applicationRepository.findByIdAndRoundPart_Round(request.applicationId(), round)
                .orElseThrow(() -> new CustomException(ErrorCode.RECRUITMENT_APPLICATION_NOT_FOUND,
                        "현재 회차에 제출된 지원서가 아닙니다. applicationId=" + request.applicationId()));
        String message = resultMessageRenderer.render(request.text(), application.getApplicant(),
                resultRepository.findByApplication(application).orElse(null));

        return new ResultTemplatePreviewResponse(application.getId(), message);
    }

    private void validateNotReserved(Round round, ResultTemplateType type) {
        LocalDateTime now = LocalDateTime.now();
        boolean locked = type.isFinalStage() ? round.isFinalResultLocked(now) : round.isDocumentResultLocked(now);
        if (locked) {
            throw new CustomException(ErrorCode.RECRUITMENT_RESULT_LOCKED,
                    type.getLabel() + " 문구는 결과 공개 예약 중에는 수정할 수 없습니다. 예약을 취소한 후 수정해주세요.");
        }
    }

    /** 면접 변수는 서류 합격 템플릿에서만 쓸 수 있다 (버튼 비활성과 같은 규칙을 서버에서도 막는다) */
    private void validateVariables(ResultTemplateType type, String text) {
        List<String> notAllowed = Arrays.stream(ResultTemplateVariable.values())
                .filter(variable -> !type.allows(variable) && text.contains(variable.getToken()))
                .map(ResultTemplateVariable::getToken)
                .toList();
        if (!notAllowed.isEmpty()) {
            throw new CustomException(ErrorCode.RECRUITMENT_INVALID_RESULT_TEMPLATE,
                    type.getLabel() + " 템플릿에서는 면접 변수를 사용할 수 없습니다. variables=" + notAllowed);
        }
    }
}

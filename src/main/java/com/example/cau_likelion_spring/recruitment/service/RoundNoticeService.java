package com.example.cau_likelion_spring.recruitment.service;

import com.example.cau_likelion_spring.global.exception.CustomException;
import com.example.cau_likelion_spring.global.exception.ErrorCode;
import com.example.cau_likelion_spring.recruitment.domain.FAQ;
import com.example.cau_likelion_spring.recruitment.domain.Round;
import com.example.cau_likelion_spring.recruitment.dto.RoundNoticeRequest;
import com.example.cau_likelion_spring.recruitment.dto.RoundNoticeResponse;
import com.example.cau_likelion_spring.recruitment.repository.RoundFaqRepository;
import com.example.cau_likelion_spring.recruitment.repository.RoundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 모집공고 관리 (일정 + FAQ).
 * 회차와 공고가 1:1이라 공고 = Round. 공고를 등록하면 새 회차가 생기고, 그 회차가 현재 회차가 된다
 * (현재 회차 = 가장 최근에 만든 회차, CurrentRoundFinder 참고).
 *
 * 관리자 화면에서 일정과 FAQ 목록을 한 번에 저장하므로 한 트랜잭션에서 함께 처리한다
 * (하나라도 실패하면 전부 롤백 → FAQ 없는 공고가 생기지 않음).
 * FAQ는 저장할 때마다 회차의 기존 FAQ를 모두 지우고 요청 순서대로 새로 만든다
 * (FAQ를 참조하는 다른 엔티티가 없어 id가 바뀌어도 영향 없음).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoundNoticeService {

    private final RoundRepository roundRepository;
    private final RoundFaqRepository roundFaqRepository;
    private final CurrentRoundFinder currentRoundFinder;

    @Transactional
    public RoundNoticeResponse createNotice(RoundNoticeRequest request) {
        validateSchedule(request);

        Round round = roundRepository.save(Round.builder()
                .startDate(request.startDate())
                .deadLine(request.deadLine())
                .documentResultDate(request.documentResultDate())
                .interviewStartDate(request.interviewStartDate())
                .interviewDeadLine(request.interviewDeadLine())
                .otDate(request.otDate())
                .build());

        List<FAQ> faqs = saveFaqs(round, request.faqs());
        return RoundNoticeResponse.of(round, faqs);
    }

    /** 현재 회차의 모집공고 */
    public RoundNoticeResponse getCurrentNotice() {
        Round round = currentRoundFinder.find();
        return RoundNoticeResponse.of(round, roundFaqRepository.findAllByRoundOrderByDisplayOrderAsc(round));
    }

    @Transactional
    public RoundNoticeResponse updateNotice(Long roundId, RoundNoticeRequest request) {
        validateSchedule(request);

        Round round = roundRepository.findById(roundId)
                .orElseThrow(() -> new CustomException(ErrorCode.RECRUITMENT_ROUND_NOT_FOUND,
                        "존재하지 않는 모집 회차입니다. roundId=" + roundId));

        round.updateNoticeSchedule(request.startDate(), request.deadLine(),
                request.documentResultDate(), request.interviewStartDate(), request.interviewDeadLine(),
                request.otDate());

        // deleteAllByRound가 flush + clear하므로, 위 일정 변경이 먼저 반영된 뒤 FAQ를 교체한다
        roundFaqRepository.deleteAllByRound(round);
        List<FAQ> faqs = saveFaqs(round, request.faqs());

        return RoundNoticeResponse.of(round, faqs);
    }

    /** 요청 순서대로 노출 순서(1부터)를 매겨 저장한다 */
    private List<FAQ> saveFaqs(Round round, List<RoundNoticeRequest.FaqItem> items) {
        List<FAQ> faqs = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            RoundNoticeRequest.FaqItem item = items.get(i);
            faqs.add(FAQ.builder()
                    .round(round)
                    .question(item.question())
                    .answer(item.answer())
                    .displayOrder(i + 1)
                    .build());
        }
        return roundFaqRepository.saveAll(faqs);
    }

    /** 서류접수 시작 < 서류접수 마감 < 서류 결과발표 < 면접 시작 < 면접 종료 < OT */
    private void validateSchedule(RoundNoticeRequest request) {
        if (!request.startDate().isBefore(request.deadLine())) {
            throw new CustomException(ErrorCode.RECRUITMENT_INVALID_ROUND_SCHEDULE,
                    "서류접수 마감일시는 시작일시 이후여야 합니다.");
        }
        if (!request.deadLine().isBefore(request.documentResultDate())) {
            throw new CustomException(ErrorCode.RECRUITMENT_INVALID_ROUND_SCHEDULE,
                    "서류 결과발표 일시는 서류접수 마감일시 이후여야 합니다.");
        }
        if (!request.documentResultDate().isBefore(request.interviewStartDate())) {
            throw new CustomException(ErrorCode.RECRUITMENT_INVALID_ROUND_SCHEDULE,
                    "면접 시작일시는 서류 결과발표 일시 이후여야 합니다.");
        }
        if (!request.interviewStartDate().isBefore(request.interviewDeadLine())) {
            throw new CustomException(ErrorCode.RECRUITMENT_INVALID_ROUND_SCHEDULE,
                    "면접 종료일시는 면접 시작일시 이후여야 합니다.");
        }
        if (!request.interviewDeadLine().isBefore(request.otDate())) {
            throw new CustomException(ErrorCode.RECRUITMENT_INVALID_ROUND_SCHEDULE,
                    "OT 일시는 면접 종료일시 이후여야 합니다.");
        }
    }
}

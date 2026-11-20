package com.example.cau_likelion_spring.recruitment.service;

import com.example.cau_likelion_spring.global.exception.CustomException;
import com.example.cau_likelion_spring.global.exception.ErrorCode;
import com.example.cau_likelion_spring.recruitment.domain.Round;
import com.example.cau_likelion_spring.recruitment.dto.RoundNoticeRequest;
import com.example.cau_likelion_spring.recruitment.dto.RoundNoticeResponse;
import com.example.cau_likelion_spring.recruitment.repository.RoundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 모집공고 관리.
 * 회차와 공고가 1:1이라 공고 = Round. 공고를 등록하면 새 회차가 생기고, 그 회차가 현재 회차가 된다
 * (현재 회차 = 가장 최근에 만든 회차, CurrentRoundFinder 참고).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoundNoticeService {

    private final RoundRepository roundRepository;
    private final CurrentRoundFinder currentRoundFinder;

    @Transactional
    public RoundNoticeResponse createNotice(RoundNoticeRequest request) {
        validateSchedule(request);

        Round round = Round.builder()
                .startDate(request.startDate())
                .deadLine(request.deadLine())
                .documentResultDate(request.documentResultDate())
                .otDate(request.otDate())
                .build();

        return RoundNoticeResponse.from(roundRepository.save(round));
    }

    /** 현재 회차의 모집공고 */
    public RoundNoticeResponse getCurrentNotice() {
        return RoundNoticeResponse.from(currentRoundFinder.find());
    }

    @Transactional
    public RoundNoticeResponse updateNotice(Long roundId, RoundNoticeRequest request) {
        validateSchedule(request);

        Round round = roundRepository.findById(roundId)
                .orElseThrow(() -> new CustomException(ErrorCode.RECRUITMENT_ROUND_NOT_FOUND,
                        "존재하지 않는 모집 회차입니다. roundId=" + roundId));

        round.updateNoticeSchedule(request.startDate(), request.deadLine(),
                request.documentResultDate(), request.otDate());
        // 영속 상태(더티 체킹)라 별도 save() 호출 불필요

        return RoundNoticeResponse.from(round);
    }

    /** 서류접수 시작 < 서류접수 마감 < 서류 결과발표 < OT */
    private void validateSchedule(RoundNoticeRequest request) {
        if (!request.startDate().isBefore(request.deadLine())) {
            throw new CustomException(ErrorCode.RECRUITMENT_INVALID_ROUND_SCHEDULE,
                    "서류접수 마감일시는 시작일시 이후여야 합니다.");
        }
        if (!request.deadLine().isBefore(request.documentResultDate())) {
            throw new CustomException(ErrorCode.RECRUITMENT_INVALID_ROUND_SCHEDULE,
                    "서류 결과발표 일시는 서류접수 마감일시 이후여야 합니다.");
        }
        if (!request.documentResultDate().isBefore(request.otDate())) {
            throw new CustomException(ErrorCode.RECRUITMENT_INVALID_ROUND_SCHEDULE,
                    "OT 일시는 서류 결과발표 일시 이후여야 합니다.");
        }
    }
}

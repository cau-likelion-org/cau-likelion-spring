package com.example.cau_likelion_spring.recruitment.service;

import com.example.cau_likelion_spring.global.exception.CustomException;
import com.example.cau_likelion_spring.global.exception.ErrorCode;
import com.example.cau_likelion_spring.recruitment.domain.Round;
import com.example.cau_likelion_spring.recruitment.dto.DashboardResponse;
import com.example.cau_likelion_spring.recruitment.repository.RoundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 리크루팅 대시보드 조회 (7.9.1).
 *
 * | 회차 상태                          | 보여주는 값                                  |
 * | 결과 확인 종료 전 (또는 종료일 미정) | 원본에서 실시간 집계 (저장 안 함)              |
 * | 결과 확인 종료 후                   | 스냅샷 (없으면 이때 한 번 만들어 저장)          |
 *
 * 스케줄러 없이 조회 시점에 판단한다.
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final RoundRepository roundRepository;
    private final CurrentRoundFinder currentRoundFinder;
    private final DashboardStatsCollector dashboardStatsCollector;
    private final StatsSnapshotService statsSnapshotService;

    /** roundId가 없으면 현재 회차 */
    @Transactional
    public DashboardResponse getDashboard(Long roundId) {
        Round round = roundId == null ? currentRoundFinder.find() : findRound(roundId);

        if (isClosed(round, LocalDateTime.now())) {
            return DashboardResponse.of(round, statsSnapshotService.createSnapshotIfAbsent(round), true);
        }
        return DashboardResponse.of(round, dashboardStatsCollector.collect(round), false);
    }

    /** 결과 확인 종료일이 지났으면 회차 종료 (종료일은 최종 결과 예약 때 정해지고, 그 전엔 진행 중으로 본다) */
    private boolean isClosed(Round round, LocalDateTime now) {
        return round.getResultCheckDeadLine() != null && now.isAfter(round.getResultCheckDeadLine());
    }

    private Round findRound(Long roundId) {
        return roundRepository.findById(roundId)
                .orElseThrow(() -> new CustomException(ErrorCode.RECRUITMENT_ROUND_NOT_FOUND,
                        "존재하지 않는 모집 회차입니다. roundId=" + roundId));
    }
}

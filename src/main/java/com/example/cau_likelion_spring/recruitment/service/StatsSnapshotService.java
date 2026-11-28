package com.example.cau_likelion_spring.recruitment.service;

import com.example.cau_likelion_spring.global.exception.CustomException;
import com.example.cau_likelion_spring.global.exception.ErrorCode;
import com.example.cau_likelion_spring.recruitment.domain.Round;
import com.example.cau_likelion_spring.recruitment.domain.StatsSnapshot;
import com.example.cau_likelion_spring.recruitment.repository.RoundRepository;
import com.example.cau_likelion_spring.recruitment.repository.StatsSnapshotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 리크루팅 통계 스냅샷 (회차당 한 번만 생성, 이후 고정).
 *
 * 결과 확인 종료 후에는 원본 데이터가 늘거나 바뀌지 않고 파기로 줄어들기만 하므로,
 * 처음 만든 스냅샷이 가장 완전한 값이다 → 한 번 만들면 다시 만들거나 덮어쓰지 않는다.
 *
 * 생성 시점
 * - 결과 확인 종료 후 대시보드를 처음 조회할 때 (DashboardService)
 * - 지원 데이터 파기 직전 (파기 로직이 원본 삭제 전에 같은 트랜잭션에서 createSnapshotIfAbsent를 호출해야 함)
 */
@Service
@RequiredArgsConstructor
public class StatsSnapshotService {

    private final RoundRepository roundRepository;
    private final StatsSnapshotRepository statsSnapshotRepository;
    private final DashboardStatsCollector dashboardStatsCollector;

    /**
     * 회차 스냅샷이 없으면 원본으로 집계해 저장하고, 있으면 저장된 것을 그대로 반환한다.
     * 동시에 두 요청이 처음 조회해도 한 세트만 생기도록 회차 행에 쓰기 락을 건 뒤 다시 확인한다.
     * 지원 데이터 파기 로직은 원본을 지우기 전에 같은 트랜잭션에서 이 메서드를 호출해야 한다.
     */
    @Transactional
    public List<StatsSnapshot> createSnapshotIfAbsent(Round round) {
        Round locked = roundRepository.findByIdForUpdate(round.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.RECRUITMENT_ROUND_NOT_FOUND,
                        "존재하지 않는 모집 회차입니다. roundId=" + round.getId()));

        if (statsSnapshotRepository.existsByRound(locked)) {
            return statsSnapshotRepository.findAllByRound(locked);
        }
        return statsSnapshotRepository.saveAll(dashboardStatsCollector.collect(locked));
    }
}

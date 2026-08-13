package com.example.cau_likelion_spring.intro.service;

import com.example.cau_likelion_spring.intro.domain.Indicator;
import com.example.cau_likelion_spring.intro.dto.IndicatorRequestDto;
import com.example.cau_likelion_spring.intro.dto.IndicatorResponseDto;
import com.example.cau_likelion_spring.intro.repository.IndicatorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IndicatorService {

    private static final String DEFAULT_VALUE = "0";

    private final IndicatorRepository indicatorRepository;

    // 최초 조회 시 row를 새로 만들 수도 있으므로 readOnly가 아닌 쓰기 트랜잭션으로 오버라이드
    @Transactional
    public IndicatorResponseDto getIndicator() {
        return IndicatorResponseDto.from(getOrCreateIndicator());
    }

    @Transactional
    public IndicatorResponseDto updateIndicator(IndicatorRequestDto request) {
        Indicator indicator = getOrCreateIndicator();
        indicator.update(
                request.getCumulativeGenerations(),
                request.getCumulativeGraduates(),
                request.getCumulativeProjects()
        );
        return IndicatorResponseDto.from(indicator);
    }

    /**
     * Indicator는 항상 단 하나만 존재해야 한다.
     * 조회 시점에 row가 없으면(=최초 조회) 기본값 0으로 새로 생성해서 반환한다.
     */
    private Indicator getOrCreateIndicator() {
        return indicatorRepository.findFirstByOrderByIdAsc()
                .orElseGet(() -> indicatorRepository.save(
                        Indicator.builder()
                                .cumulative_generations(DEFAULT_VALUE)
                                .cumulative_graduates(DEFAULT_VALUE)
                                .cumulative_projects(DEFAULT_VALUE)
                                .build()
                ));
    }
}

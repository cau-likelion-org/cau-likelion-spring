package com.example.cau_likelion_spring.recruitment.service;

import com.example.cau_likelion_spring.global.exception.CustomException;
import com.example.cau_likelion_spring.global.exception.ErrorCode;
import com.example.cau_likelion_spring.recruitment.domain.Round;
import com.example.cau_likelion_spring.recruitment.repository.RoundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 현재 회차 조회 (지원서·면접 시간 등 여러 서비스에서 공통으로 사용).
 * 현재 회차 = 가장 최근에 만든 회차.
 */
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CurrentRoundFinder {

    private final RoundRepository roundRepository;

    public Round find() {
        return roundRepository.findTopByOrderByIdDesc()
                .orElseThrow(() -> new CustomException(ErrorCode.RECRUITMENT_ROUND_NOT_FOUND, "진행 중인 모집 회차가 없습니다."));
    }
}

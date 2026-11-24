package com.example.cau_likelion_spring.recruitment.service;

import com.example.cau_likelion_spring.global.exception.CustomException;
import com.example.cau_likelion_spring.global.exception.ErrorCode;
import com.example.cau_likelion_spring.recruitment.domain.Round;
import com.example.cau_likelion_spring.recruitment.domain.RoundPart;
import com.example.cau_likelion_spring.recruitment.repository.RoundPartRepository;
import com.example.cau_likelion_spring.recruitment.repository.RoundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 현재 회차 조회 (지원서·면접 시간·문항 등 여러 서비스에서 공통으로 사용).
 * 현재 회차 = 가장 최근에 만든 회차.
 */
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CurrentRoundFinder {

    private final RoundRepository roundRepository;
    private final RoundPartRepository roundPartRepository;

    public Round find() {
        return roundRepository.findTopByOrderByIdDesc()
                .orElseThrow(() -> new CustomException(ErrorCode.RECRUITMENT_ROUND_NOT_FOUND, "진행 중인 모집 회차가 없습니다."));
    }

    /** 요청한 파트가 해당 회차의 파트인지 확인하고 반환한다 */
    public RoundPart findRoundPart(Round round, Long roundPartId) {
        return roundPartRepository.findById(roundPartId)
                .filter(roundPart -> roundPart.getRound() != null
                        && round.getId().equals(roundPart.getRound().getId()))
                .orElseThrow(() -> new CustomException(ErrorCode.RECRUITMENT_ROUND_PART_NOT_FOUND,
                        "현재 회차의 파트가 아닙니다. roundPartId=" + roundPartId));
    }
}

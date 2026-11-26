package com.example.cau_likelion_spring.recruitment.service;

import com.example.cau_likelion_spring.recruitment.dto.RoundSummaryResponse;
import com.example.cau_likelion_spring.recruitment.repository.RoundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoundQueryService {

    private final RoundRepository roundRepository;

    /** 전체 회차를 최근에 만든 회차 먼저 조회한다. 회차가 없으면 빈 목록이다. */
    public List<RoundSummaryResponse> getRounds() {
        return roundRepository.findAllByOrderByIdDesc().stream()
                .map(RoundSummaryResponse::from)
                .toList();
    }
}

package com.example.cau_likelion_spring.recruitment.service;

import com.example.cau_likelion_spring.organization.domain.Generation;
import com.example.cau_likelion_spring.recruitment.domain.Round;
import com.example.cau_likelion_spring.recruitment.repository.RoundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 모집 회차 생성.
 * 회차는 지금까지 중 가장 큰 번호의 기수가 생성될 때 함께 만들어진다 (GenerationService.createGeneration).
 * 만든 회차가 가장 최근 회차이므로 바로 현재 회차가 되고, 일정·FAQ는 이후 모집공고 등록 API로 채운다.
 */
@Service
@RequiredArgsConstructor
public class RoundService {

    private final RoundRepository roundRepository;

    /** 기수의 모집 회차를 일정 없이 만든다 (호출한 쪽 트랜잭션에 참여 → 기수 생성이 실패하면 함께 롤백) */
    @Transactional
    public Round createForGeneration(Generation generation) {
        return roundRepository.save(Round.builder()
                .generation(generation)
                .build());
    }
}

package com.example.cau_likelion_spring.organization.service;

import com.example.cau_likelion_spring.organization.domain.Generation;
import com.example.cau_likelion_spring.organization.domain.GenerationStatus;
import com.example.cau_likelion_spring.organization.domain.Part;
import com.example.cau_likelion_spring.organization.dto.GenerationCreateRequestDto;
import com.example.cau_likelion_spring.organization.dto.GenerationListResponseDto;
import com.example.cau_likelion_spring.organization.repository.GenerationRepository;
import com.example.cau_likelion_spring.organization.repository.PartRepository;
import com.example.cau_likelion_spring.global.exception.CustomException;
import com.example.cau_likelion_spring.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GenerationService {

    private final GenerationRepository generationRepository;
    private final PartRepository partRepository;

    @Transactional
    public GenerationListResponseDto createGeneration(GenerationCreateRequestDto request) {
        Generation generation = Generation.builder()
                .number(request.getNumber())
                .year(request.getYear())
                .status(GenerationStatus.BEFORE_ACTIVITY) // 새로 만든 기수는 기본적으로 "활동 전" - 별도로 activateGeneration 호출해야 활동 중으로 전환됨
                .build();
        Generation savedGeneration = generationRepository.save(generation);

        List<Part> parts = request.getPartNames().stream()
                .map(partName -> Part.builder()
                        .generation(savedGeneration)
                        .name(partName)
                        .build())
                .toList();
        List<Part> savedParts = partRepository.saveAll(parts);

        return GenerationListResponseDto.of(savedGeneration, savedParts);
    }

    public List<GenerationListResponseDto> getGenerationList() {
        List<Generation> generations = generationRepository.findAll();

        List<Long> generationIds = generations.stream()
                .map(Generation::getId)
                .toList();

        // 기수 N개 조회하고 파트는 한 번의 쿼리로 다 가져와서 메모리에서 묶음 (N+1 방지)
        Map<Long, List<Part>> partsByGenerationId = partRepository.findAllByGeneration_IdIn(generationIds).stream()
                .collect(Collectors.groupingBy(part -> part.getGeneration().getId()));

        return generations.stream()
                .map(generation -> GenerationListResponseDto.of(
                        generation,
                        partsByGenerationId.getOrDefault(generation.getId(), List.of())
                ))
                .toList();
    }

    /**
     * 대상 기수를 "활동 중"으로 전환한다.
     * 기존에 "활동 중"이었던 기수가 있다면 "활동 후"로 바뀐다.
     * (전체 기수를 다 순회하지 않고, 관련된 최대 2개 기수만 건드림)
     */
    @Transactional
    public void changeCurrentGeneration(Long id) {
        Generation target = generationRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.GENERATION_NOT_FOUND, "존재하지 않는 기수입니다. id=" + id));

        generationRepository.findByStatus(GenerationStatus.IN_ACTIVITY)
                .ifPresent(previous -> previous.changeStatus(GenerationStatus.AFTER_ACTIVITY));

        target.changeStatus(GenerationStatus.IN_ACTIVITY);
        // 영속 상태(더티 체킹)라 별도 save() 호출 불필요
    }

    /**
     * 현재 기수 판단 - status가 IN_ACTIVITY인 Generation을 반환
     * 다른 도메인(session, assignment 등)에서 "현재 기수"가 필요할 때 이 메서드를 재사용하면 된다.
     */
    public Generation getCurrentGeneration() {
        return generationRepository.findByStatus(GenerationStatus.IN_ACTIVITY)
                .orElseThrow(() -> new CustomException(ErrorCode.CURRENT_GENERATION_NOT_FOUND, "현재 기수로 지정된 기수가 없습니다."));
    }
}

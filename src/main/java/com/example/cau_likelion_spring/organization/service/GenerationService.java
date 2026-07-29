package com.example.cau_likelion_spring.organization.service;

import com.example.cau_likelion_spring.organization.domain.Generation;
import com.example.cau_likelion_spring.organization.domain.Part;
import com.example.cau_likelion_spring.organization.dto.GenerationCreateRequestDto;
import com.example.cau_likelion_spring.organization.dto.GenerationListResponseDto;
import com.example.cau_likelion_spring.organization.repository.GenerationRepository;
import com.example.cau_likelion_spring.organization.repository.PartRepository;
import jakarta.persistence.EntityNotFoundException;
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
                .isCurrent(false) // 새로 만든 기수를 자동으로 현재 기수로 만들지 않음 - 별도로 changeCurrentGeneration 호출해야 함
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
     * 현재 기수를 전환한다.
     * 대상 기수를 isCurrent = true로, 나머지 모든 기수를 isCurrent = false로 만든다.
     */
    @Transactional
    public void changeCurrentGeneration(Long id) {
        List<Generation> generations = generationRepository.findAll();

        boolean targetExists = generations.stream().anyMatch(g -> g.getId().equals(id));
        if (!targetExists) {
            throw new EntityNotFoundException("존재하지 않는 기수입니다. id=" + id);
        }

        for (Generation generation : generations) {
            generation.changeCurrent(generation.getId().equals(id));
        }
        // 영속 상태(더티 체킹)라 별도 save() 호출 불필요
    }

    /**
     * 현재 기수 판단 - isCurrent가 true인 Generation을 반환
     * 다른 도메인(session, assignment 등)에서 "현재 기수"가 필요할 때 이 메서드를 재사용하면 된다.
     */
    public Generation getCurrentGeneration() {
        return generationRepository.findByIsCurrentTrue()
                .orElseThrow(() -> new EntityNotFoundException("현재 기수로 지정된 기수가 없습니다."));
    }
}

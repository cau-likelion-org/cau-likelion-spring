package com.example.cau_likelion_spring.organization.service;

import com.example.cau_likelion_spring.organization.domain.Generation;
import com.example.cau_likelion_spring.organization.domain.Part;
import com.example.cau_likelion_spring.organization.dto.GenerationListResponseDto;
import com.example.cau_likelion_spring.organization.repository.GenerationRepository;
import com.example.cau_likelion_spring.organization.repository.PartRepository;
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
}

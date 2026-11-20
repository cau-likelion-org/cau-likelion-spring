package com.example.cau_likelion_spring.recruitment.service;

import com.example.cau_likelion_spring.global.exception.CustomException;
import com.example.cau_likelion_spring.global.exception.ErrorCode;
import com.example.cau_likelion_spring.organization.domain.Generation;
import com.example.cau_likelion_spring.organization.domain.Part;
import com.example.cau_likelion_spring.organization.repository.GenerationRepository;
import com.example.cau_likelion_spring.organization.repository.PartRepository;
import com.example.cau_likelion_spring.recruitment.domain.Round;
import com.example.cau_likelion_spring.recruitment.domain.RoundPart;
import com.example.cau_likelion_spring.recruitment.dto.RoundPartHeadCountRequest;
import com.example.cau_likelion_spring.recruitment.dto.RoundPartHeadCountResponse;
import com.example.cau_likelion_spring.recruitment.repository.RoundPartRepository;
import com.example.cau_likelion_spring.recruitment.repository.RoundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 모집 기수의 파트별 정원 관리 (운영자용).
 * 기수 번호를 받으면 그 기수(organization.Generation)의 파트를 불러오고,
 * 현재 회차에 파트별 정원(RoundPart)을 등록·수정한다.
 *
 * RoundPart.part는 Part FK가 아니라 파트명 문자열이라, 같은 회차·기수 안에서 파트명으로 매칭한다.
 * 이미 지원서·문항이 RoundPart를 참조할 수 있으므로 수정 시 삭제 후 재생성하지 않고 정원만 바꾼다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoundPartService {

    private final GenerationRepository generationRepository;
    private final PartRepository partRepository;
    private final RoundRepository roundRepository;
    private final RoundPartRepository roundPartRepository;
    private final CurrentRoundFinder currentRoundFinder;

    /** 기수의 파트 목록과 현재 회차에 등록된 정원을 함께 조회한다 (회차가 없으면 정원은 모두 null) */
    public RoundPartHeadCountResponse getHeadCounts(Integer generationNumber) {
        Generation generation = findGeneration(generationNumber);
        Round round = roundRepository.findTopByOrderByIdDesc().orElse(null);
        return buildResponse(generation, round);
    }

    /** 요청한 파트의 정원을 등록(없으면 생성)하거나 수정한다 */
    @Transactional
    public RoundPartHeadCountResponse upsertHeadCounts(Integer generationNumber, RoundPartHeadCountRequest request) {
        Generation generation = findGeneration(generationNumber);
        Round round = currentRoundFinder.find();

        validateNoDuplicatePart(request);

        Map<Long, Part> partById = partRepository.findAllByGeneration_Id(generation.getId()).stream()
                .collect(Collectors.toMap(Part::getId, Function.identity()));
        Map<String, RoundPart> roundPartByName = findRoundPartsByName(round, generation);

        for (RoundPartHeadCountRequest.PartHeadCountItem item : request.getParts()) {
            Part part = partById.get(item.getPartId());
            if (part == null) {
                throw new CustomException(ErrorCode.PART_NOT_FOUND,
                        generation.getNumber() + "기의 파트가 아닙니다. partId=" + item.getPartId());
            }

            RoundPart existing = roundPartByName.get(part.getName());
            if (existing != null) {
                existing.changeHeadCount(item.getHeadCount());
            } else {
                roundPartRepository.save(RoundPart.builder()
                        .round(round)
                        .part(part.getName())
                        .headCount(item.getHeadCount())
                        .generation(generation.getNumber())
                        .build());
            }
        }

        return buildResponse(generation, round);
    }

    private Generation findGeneration(Integer generationNumber) {
        return generationRepository.findFirstByNumberOrderByIdDesc(generationNumber)
                .orElseThrow(() -> new CustomException(ErrorCode.GENERATION_NOT_FOUND,
                        "존재하지 않는 기수입니다. generation=" + generationNumber));
    }

    private void validateNoDuplicatePart(RoundPartHeadCountRequest request) {
        Set<Long> seen = new HashSet<>();
        for (RoundPartHeadCountRequest.PartHeadCountItem item : request.getParts()) {
            if (!seen.add(item.getPartId())) {
                throw new CustomException(ErrorCode.RECRUITMENT_DUPLICATE_PART_HEAD_COUNT,
                        "같은 파트가 중복으로 요청되었습니다. partId=" + item.getPartId());
            }
        }
    }

    private Map<String, RoundPart> findRoundPartsByName(Round round, Generation generation) {
        return roundPartRepository.findAllByRoundAndGeneration(round, generation.getNumber()).stream()
                .collect(Collectors.toMap(RoundPart::getPart, Function.identity(), (first, second) -> first));
    }

    private RoundPartHeadCountResponse buildResponse(Generation generation, Round round) {
        Map<String, RoundPart> roundPartByName = round == null ? Map.of() : findRoundPartsByName(round, generation);

        List<RoundPartHeadCountResponse.PartHeadCountDetail> parts = partRepository.findAllByGeneration_Id(generation.getId())
                .stream()
                .sorted(Comparator.comparing(Part::getId))
                .map(part -> RoundPartHeadCountResponse.PartHeadCountDetail.of(part, roundPartByName.get(part.getName())))
                .toList();

        return RoundPartHeadCountResponse.builder()
                .generation(generation.getNumber())
                .roundId(round == null ? null : round.getId())
                .parts(parts)
                .build();
    }
}

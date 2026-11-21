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

import java.util.ArrayList;
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
 * 현재 회차에 파트별 정원(RoundPart)을 등록(POST)·수정(PATCH)한다.
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

    /**
     * 파트별 정원을 처음 등록한다.
     * 요청한 파트 중 하나라도 현재 회차에 이미 등록돼 있으면 전체를 거부한다 (수정은 PATCH).
     */
    @Transactional
    public RoundPartHeadCountResponse createHeadCounts(Integer generationNumber, RoundPartHeadCountRequest request) {
        Generation generation = findGeneration(generationNumber);
        Round round = currentRoundFinder.find();
        validateNoDuplicatePart(request);

        Map<Long, Part> partById = findPartsById(generation);
        Map<String, RoundPart> roundPartByName = findRoundPartsByName(round, generation);

        // 검증을 먼저 끝내고 저장한다 (일부만 저장되는 상황 방지)
        List<Part> targetParts = request.getParts().stream()
                .map(item -> findPart(partById, generation, item.getPartId()))
                .toList();
        for (Part part : targetParts) {
            if (roundPartByName.containsKey(part.getName())) {
                throw new CustomException(ErrorCode.RECRUITMENT_ROUND_PART_ALREADY_EXISTS,
                        "이미 정원이 등록된 파트입니다. 수정은 PATCH를 사용하세요. partId=" + part.getId());
            }
        }

        List<RoundPart> newRoundParts = new ArrayList<>();
        for (int i = 0; i < targetParts.size(); i++) {
            Part part = targetParts.get(i);
            newRoundParts.add(RoundPart.builder()
                    .round(round)
                    .part(part.getName())
                    .headCount(request.getParts().get(i).getHeadCount())
                    .generation(generation.getNumber())
                    .build());
        }
        roundPartRepository.saveAll(newRoundParts);

        return buildResponse(generation, round);
    }

    /**
     * 이미 등록된 파트의 정원을 수정한다.
     * 요청한 파트 중 하나라도 현재 회차에 등록돼 있지 않으면 전체를 거부한다 (등록은 POST).
     */
    @Transactional
    public RoundPartHeadCountResponse updateHeadCounts(Integer generationNumber, RoundPartHeadCountRequest request) {
        Generation generation = findGeneration(generationNumber);
        Round round = currentRoundFinder.find();
        validateNoDuplicatePart(request);

        Map<Long, Part> partById = findPartsById(generation);
        Map<String, RoundPart> roundPartByName = findRoundPartsByName(round, generation);

        // 검증을 먼저 끝내고 수정한다 (일부만 수정되는 상황 방지)
        List<RoundPart> targets = request.getParts().stream()
                .map(item -> {
                    Part part = findPart(partById, generation, item.getPartId());
                    RoundPart roundPart = roundPartByName.get(part.getName());
                    if (roundPart == null) {
                        throw new CustomException(ErrorCode.RECRUITMENT_ROUND_PART_NOT_FOUND,
                                "정원이 등록되지 않은 파트입니다. 등록은 POST를 사용하세요. partId=" + part.getId());
                    }
                    return roundPart;
                })
                .toList();

        for (int i = 0; i < targets.size(); i++) {
            targets.get(i).changeHeadCount(request.getParts().get(i).getHeadCount());
        }
        // 영속 상태(더티 체킹)라 별도 save() 호출 불필요

        return buildResponse(generation, round);
    }

    private Map<Long, Part> findPartsById(Generation generation) {
        return partRepository.findAllByGeneration_Id(generation.getId()).stream()
                .collect(Collectors.toMap(Part::getId, Function.identity()));
    }

    private Part findPart(Map<Long, Part> partById, Generation generation, Long partId) {
        Part part = partById.get(partId);
        if (part == null) {
            throw new CustomException(ErrorCode.PART_NOT_FOUND,
                    generation.getNumber() + "기의 파트가 아닙니다. partId=" + partId);
        }
        return part;
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

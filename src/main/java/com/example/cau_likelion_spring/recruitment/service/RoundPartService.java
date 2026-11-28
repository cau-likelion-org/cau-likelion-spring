package com.example.cau_likelion_spring.recruitment.service;

import com.example.cau_likelion_spring.global.exception.CustomException;
import com.example.cau_likelion_spring.global.exception.ErrorCode;
import com.example.cau_likelion_spring.organization.domain.Generation;
import com.example.cau_likelion_spring.organization.domain.Part;
import com.example.cau_likelion_spring.organization.repository.PartRepository;
import com.example.cau_likelion_spring.recruitment.domain.Round;
import com.example.cau_likelion_spring.recruitment.domain.RoundPart;
import com.example.cau_likelion_spring.recruitment.dto.RoundPartHeadCountRequest;
import com.example.cau_likelion_spring.recruitment.dto.RoundPartHeadCountResponse;
import com.example.cau_likelion_spring.recruitment.repository.RoundPartRepository;
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
 * 현재 회차의 파트별 정원 관리 (운영자용).
 * 현재 회차에 연결된 기수(기수 생성 시 자동 연결, RoundService)의 파트를 불러오고,
 * 파트별 정원(RoundPart)을 등록(POST)·수정(PATCH)한다.
 *
 * RoundPart.part는 Part FK가 아니라 파트명 문자열이라, 같은 회차 안에서 파트명으로 매칭한다.
 * 이미 지원서·문항이 RoundPart를 참조할 수 있으므로 수정 시 삭제 후 재생성하지 않고 정원만 바꾼다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoundPartService {

    private final PartRepository partRepository;
    private final RoundPartRepository roundPartRepository;
    private final CurrentRoundFinder currentRoundFinder;

    /** 현재 회차 기수의 파트 목록과 등록된 정원을 함께 조회한다 (정원 미등록 파트는 null) */
    public RoundPartHeadCountResponse getHeadCounts() {
        Round round = currentRoundFinder.find();
        return buildResponse(round, findGeneration(round));
    }

    /**
     * 파트별 정원을 처음 등록한다.
     * 요청한 파트 중 하나라도 현재 회차에 이미 등록돼 있으면 전체를 거부한다 (수정은 PATCH).
     */
    @Transactional
    public RoundPartHeadCountResponse createHeadCounts(RoundPartHeadCountRequest request) {
        Round round = currentRoundFinder.find();
        Generation generation = findGeneration(round);
        validateNoDuplicatePart(request);

        Map<Long, Part> partById = findPartsById(generation);
        Map<String, RoundPart> roundPartByName = findRoundPartsByName(round);

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
            newRoundParts.add(RoundPart.builder()
                    .round(round)
                    .part(targetParts.get(i).getName())
                    .headCount(request.getParts().get(i).getHeadCount())
                    .build());
        }
        roundPartRepository.saveAll(newRoundParts);

        return buildResponse(round, generation);
    }

    /**
     * 이미 등록된 파트의 정원을 수정한다.
     * 요청한 파트 중 하나라도 현재 회차에 등록돼 있지 않으면 전체를 거부한다 (등록은 POST).
     */
    @Transactional
    public RoundPartHeadCountResponse updateHeadCounts(RoundPartHeadCountRequest request) {
        Round round = currentRoundFinder.find();
        Generation generation = findGeneration(round);
        validateNoDuplicatePart(request);

        Map<Long, Part> partById = findPartsById(generation);
        Map<String, RoundPart> roundPartByName = findRoundPartsByName(round);

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

        return buildResponse(round, generation);
    }

    /** 현재 회차에 연결된 기수 (기수 생성 전에 만들어진 예전 회차는 비어 있을 수 있다) */
    private Generation findGeneration(Round round) {
        Generation generation = round.getGeneration();
        if (generation == null) {
            throw new CustomException(ErrorCode.GENERATION_NOT_FOUND,
                    "현재 회차에 연결된 기수가 없습니다. roundId=" + round.getId());
        }
        return generation;
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

    private void validateNoDuplicatePart(RoundPartHeadCountRequest request) {
        Set<Long> seen = new HashSet<>();
        for (RoundPartHeadCountRequest.PartHeadCountItem item : request.getParts()) {
            if (!seen.add(item.getPartId())) {
                throw new CustomException(ErrorCode.RECRUITMENT_DUPLICATE_PART_HEAD_COUNT,
                        "같은 파트가 중복으로 요청되었습니다. partId=" + item.getPartId());
            }
        }
    }

    private Map<String, RoundPart> findRoundPartsByName(Round round) {
        return roundPartRepository.findAllByRoundOrderByIdAsc(round).stream()
                .collect(Collectors.toMap(RoundPart::getPart, Function.identity(), (first, second) -> first));
    }

    private RoundPartHeadCountResponse buildResponse(Round round, Generation generation) {
        Map<String, RoundPart> roundPartByName = findRoundPartsByName(round);

        List<RoundPartHeadCountResponse.PartHeadCountDetail> parts = partRepository.findAllByGeneration_Id(generation.getId())
                .stream()
                .sorted(Comparator.comparing(Part::getId))
                .map(part -> RoundPartHeadCountResponse.PartHeadCountDetail.of(part, roundPartByName.get(part.getName())))
                .toList();

        return RoundPartHeadCountResponse.builder()
                .generation(generation.getNumber())
                .roundId(round.getId())
                .parts(parts)
                .build();
    }
}

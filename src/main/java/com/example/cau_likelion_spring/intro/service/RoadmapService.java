package com.example.cau_likelion_spring.intro.service;

import com.example.cau_likelion_spring.global.exception.CustomException;
import com.example.cau_likelion_spring.global.exception.ErrorCode;
import com.example.cau_likelion_spring.intro.domain.Roadmap;
import com.example.cau_likelion_spring.intro.dto.RoadmapRequestDto;
import com.example.cau_likelion_spring.intro.dto.RoadmapResponseDto;
import com.example.cau_likelion_spring.intro.repository.RoadmapRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoadmapService {

    private final RoadmapRepository roadmapRepository;

    @Transactional
    public RoadmapResponseDto addRoadmap(RoadmapRequestDto request) {
        Roadmap roadmap = roadmapRepository.save(
                Roadmap.builder()
                        .imageUrl(request.getImageUrl())
                        .build()
        );
        return RoadmapResponseDto.from(roadmap);
    }

    public RoadmapResponseDto getLatestRoadmap() {
        Roadmap roadmap = roadmapRepository.findTopByOrderByIdDesc()
                .orElseThrow(() -> new CustomException(ErrorCode.ROADMAP_NOT_FOUND, "등록된 로드맵 이미지가 없습니다."));
        return RoadmapResponseDto.from(roadmap);
    }
}

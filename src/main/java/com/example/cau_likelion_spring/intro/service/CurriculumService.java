package com.example.cau_likelion_spring.intro.service;

import com.example.cau_likelion_spring.intro.domain.Curriculum;
import com.example.cau_likelion_spring.intro.domain.Track;
import com.example.cau_likelion_spring.intro.dto.CurriculumRequestDto;
import com.example.cau_likelion_spring.intro.dto.CurriculumResponseDto;
import com.example.cau_likelion_spring.intro.repository.CurriculumRepository;
import com.example.cau_likelion_spring.intro.repository.TrackRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CurriculumService {

    private final CurriculumRepository curriculumRepository;
    private final TrackRepository trackRepository;

    @Transactional
    public CurriculumResponseDto createCurriculum(CurriculumRequestDto request) {
        Track track = findTrackById(request.getTrackId());

        Curriculum curriculum = Curriculum.builder()
                .track(track)
                .week(request.getWeek())
                .title(request.getTitle())
                .description(request.getDescription())
                .build();

        return CurriculumResponseDto.from(curriculumRepository.save(curriculum));
    }

    public List<CurriculumResponseDto> getCurriculumList() {
        return curriculumRepository.findAll().stream()
                .map(CurriculumResponseDto::from)
                .toList();
    }

    @Transactional
    public CurriculumResponseDto updateCurriculum(Long id, CurriculumRequestDto request) {
        Curriculum curriculum = findCurriculumById(id);
        Track track = findTrackById(request.getTrackId());

        curriculum.update(track, request.getWeek(), request.getTitle(), request.getDescription());

        return CurriculumResponseDto.from(curriculum);
    }

    @Transactional
    public void deleteCurriculum(Long id) {
        curriculumRepository.delete(findCurriculumById(id));
    }

    private Curriculum findCurriculumById(Long id) {
        return curriculumRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 커리큘럼입니다. id=" + id));
    }

    private Track findTrackById(Long trackId) {
        return trackRepository.findById(trackId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 트랙입니다. trackId=" + trackId));
    }
}

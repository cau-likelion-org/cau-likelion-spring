package com.example.cau_likelion_spring.intro.service;

import com.example.cau_likelion_spring.intro.domain.Track;
import com.example.cau_likelion_spring.intro.dto.TrackRequestDto;
import com.example.cau_likelion_spring.intro.dto.TrackResponseDto;
import com.example.cau_likelion_spring.intro.repository.TrackRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrackService {

    private final TrackRepository trackRepository;

    @Transactional
    public TrackResponseDto createTrack(TrackRequestDto request) {
        Track track = Track.builder()
                .koName(request.getKoName())
                .enName(request.getEnName())
                .introduction(request.getIntroduction())
                .techStack(request.getTechStack())
                .build();

        return TrackResponseDto.from(trackRepository.save(track));
    }

    public List<TrackResponseDto> getTrackList() {
        return trackRepository.findAll().stream()
                .map(TrackResponseDto::from)
                .toList();
    }

    @Transactional
    public TrackResponseDto updateTrack(Long id, TrackRequestDto request) {
        Track track = findTrackById(id);
        track.update(request.getKoName(), request.getEnName(), request.getIntroduction(), request.getTechStack());
        return TrackResponseDto.from(track);
    }

    @Transactional
    public void deleteTrack(Long id) {
        trackRepository.delete(findTrackById(id));
    }

    private Track findTrackById(Long id) {
        return trackRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 트랙입니다. id=" + id));
    }
}

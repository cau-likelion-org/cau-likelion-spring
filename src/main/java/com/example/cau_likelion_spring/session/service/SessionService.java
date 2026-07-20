package com.example.cau_likelion_spring.session.service;

import com.example.cau_likelion_spring.organization.domain.Part;
import com.example.cau_likelion_spring.organization.repository.PartRepository;

import com.example.cau_likelion_spring.session.domain.Session;
import com.example.cau_likelion_spring.session.dto.SessionCreateRequestDto;
import com.example.cau_likelion_spring.session.dto.SessionListResponseDto;
import com.example.cau_likelion_spring.session.dto.SessionResponseDto;
import com.example.cau_likelion_spring.session.dto.SessionUpdateRequestDto;
import com.example.cau_likelion_spring.session.repository.SessionRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SessionService {

    private final SessionRepository sessionRepository;
    private final PartRepository partRepository;

    @Transactional
    public SessionResponseDto createSession(SessionCreateRequestDto request) {
        // 해당 기수의 해당 파트 찾기
        Part part = partRepository.findByNameAndGeneration_Number(
                        request.getPartName(), request.getGenerationNumber())
                .orElseThrow(() -> new EntityNotFoundException(
                        "존재하지 않는 파트입니다. partName=" + request.getPartName()
                                + ", generationNumber=" + request.getGenerationNumber()));

        Session session = Session.builder()
                .part(part)
                .title(request.getTitle())
                .description(request.getDescription())
                .sessionDate(request.getSessionDate())
                .degree(request.getDegree())
                .thumbnailUrl(request.getThumbnailUrl())
                .build();

        return SessionResponseDto.from(sessionRepository.save(session));
    }

    @Transactional
    public SessionResponseDto getSession(Long sessionId) {
        return SessionResponseDto.from(sessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "존재하지 않는 세션입니다. sessionId=" + sessionId
                )));
    }

    @Transactional
    public List<SessionListResponseDto> getSessionList(String partName, Integer generationNumber) {
        List<Session> sessions;

        if (partName != null && generationNumber != null) {
            sessions = sessionRepository.findByPart_NameAndPart_Generation_Number(partName, generationNumber);
        } else if (partName != null) {
            sessions = sessionRepository.findByPart_Name(partName);
        } else if (generationNumber != null) {
            sessions = sessionRepository.findByPart_Generation_Number(generationNumber);
        } else {
            sessions = sessionRepository.findAll();
        }

        List<SessionListResponseDto> sessionListResponseDtos = new ArrayList<>();
        for (Session session : sessions) {
            sessionListResponseDtos.add(SessionListResponseDto.from(session));
        }
        return sessionListResponseDtos;
    }

    @Transactional
    public SessionResponseDto updateSession(Long sessionId, SessionUpdateRequestDto request) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "존재하지 않는 세션입니다. sessionId=" + sessionId
                ));

        session.update(
                request.getTitle(), request.getDescription(), request.getSessionDate(),
                request.getDegree(), request.getThumbnailUrl()
        );

        return SessionResponseDto.from(sessionRepository.save(session));
    }s
}

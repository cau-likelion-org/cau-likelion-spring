package com.example.cau_likelion_spring.session.service;

import com.example.cau_likelion_spring.organization.domain.Part;
import com.example.cau_likelion_spring.organization.repository.PartRepository;

import com.example.cau_likelion_spring.session.domain.Session;
import com.example.cau_likelion_spring.session.domain.SessionImage;
import com.example.cau_likelion_spring.session.dto.SessionCreateRequestDto;
import com.example.cau_likelion_spring.session.dto.SessionListResponseDto;
import com.example.cau_likelion_spring.session.dto.SessionResponseDto;
import com.example.cau_likelion_spring.session.dto.SessionUpdateRequestDto;
import com.example.cau_likelion_spring.session.repository.SessionImageRepository;
import com.example.cau_likelion_spring.session.repository.SessionRepository;

import com.example.cau_likelion_spring.global.exception.CustomException;
import com.example.cau_likelion_spring.global.exception.ErrorCode;
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
    private final SessionImageRepository sessionImageRepository;
    private final PartRepository partRepository;

    @Transactional
    public SessionResponseDto createSession(SessionCreateRequestDto request) {
        // 해당 기수의 해당 파트 찾기
        Part part = partRepository.findByNameAndGeneration_Number(
                        request.getPartName(), request.getGenerationNumber())
                .orElseThrow(() -> new CustomException(ErrorCode.PART_NOT_FOUND,
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

        // 세션을 먼저 저장해서 id를 확보해야 SessionImage가 FK로 참조할 수 있음
        Session savedSession = sessionRepository.save(session);

        // 세션 이미지 추가 (imageUrls가 안 넘어올 수도 있으니 null-safe하게 처리)
        List<String> imageUrls = request.getImageUrls() != null
                ? request.getImageUrls()
                : List.of();

        List<SessionImage> sessionImages = new ArrayList<>();
        for (String imageUrl : imageUrls) {
            sessionImages.add(SessionImage.builder()
                    .session(savedSession)
                    .imageUrl(imageUrl)
                    .build());
        }
        sessionImageRepository.saveAll(sessionImages);

        return SessionResponseDto.from(savedSession, imageUrls);
    }

    @Transactional(readOnly = true)
    public SessionResponseDto getSession(Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new CustomException(ErrorCode.SESSION_NOT_FOUND,
                        "존재하지 않는 세션입니다. sessionId=" + sessionId
                ));
        return SessionResponseDto.from(session, getImageUrls(session));
    }

    @Transactional(readOnly = true)
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
                .orElseThrow(() -> new CustomException(ErrorCode.SESSION_NOT_FOUND,
                        "존재하지 않는 세션입니다. sessionId=" + sessionId
                ));

        session.update(
                request.getTitle(), request.getDescription(), request.getSessionDate(),
                request.getDegree(), request.getThumbnailUrl()
        );

        // imageUrls가 요청에 명시적으로 넘어온 경우에만 이미지 목록을 통째로 교체
        List<String> imageUrls;
        if (request.getImageUrls() != null) {
            sessionImageRepository.deleteAllBySession(session);

            List<SessionImage> newImages = new ArrayList<>();
            for (String imageUrl : request.getImageUrls()) {
                newImages.add(SessionImage.builder()
                        .session(session)
                        .imageUrl(imageUrl)
                        .build());
            }
            sessionImageRepository.saveAll(newImages);
            imageUrls = request.getImageUrls();
        } else {
            imageUrls = getImageUrls(session);
        }

        return SessionResponseDto.from(session, imageUrls);
    }

    @Transactional
    public void deleteSession(Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new CustomException(ErrorCode.SESSION_NOT_FOUND,
                        "존재하지 않는 세션입니다. sessionId=" + sessionId
                ));
        sessionImageRepository.deleteAllBySession(session);
        sessionRepository.delete(session);
    }

    private List<String> getImageUrls(Session session) {
        List<String> urls = new ArrayList<>();
        for (SessionImage image : sessionImageRepository.findAllBySession(session)) {
            urls.add(image.getImageUrl());
        }
        return urls;
    }
}

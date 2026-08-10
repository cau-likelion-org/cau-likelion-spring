package com.example.cau_likelion_spring.history.service;

import com.example.cau_likelion_spring.history.domain.History;
import com.example.cau_likelion_spring.history.domain.HistoryImage;
import com.example.cau_likelion_spring.history.dto.HistoryCreateRequest;
import com.example.cau_likelion_spring.history.dto.HistoryDetailResponse;
import com.example.cau_likelion_spring.history.dto.HistoryListResponse;
import com.example.cau_likelion_spring.history.dto.HistoryUpdateRequest;
import com.example.cau_likelion_spring.history.repository.HistoryImageRepository;
import com.example.cau_likelion_spring.history.repository.HistoryRepository;
import com.example.cau_likelion_spring.global.exception.CustomException;
import com.example.cau_likelion_spring.global.exception.ErrorCode;
import com.example.cau_likelion_spring.global.util.S3Uploader;
import com.example.cau_likelion_spring.organization.domain.Generation;
import com.example.cau_likelion_spring.organization.repository.GenerationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HistoryService {

    private final HistoryRepository historyRepository;
    private final HistoryImageRepository historyImageRepository;
    private final GenerationRepository generationRepository;
    private final S3Uploader s3Uploader;

    public List<HistoryListResponse> getList() {
        return historyRepository.findAllByOrderByStartDateDesc().stream()
                .map(HistoryListResponse::from)
                .toList();
    }

    public HistoryDetailResponse getDetail(Long id) {
        History history = getHistory(id);
        List<String> imageUrls = getImageUrls(history);
        return HistoryDetailResponse.of(history, imageUrls);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public HistoryDetailResponse create(HistoryCreateRequest request) {
        Generation generation = getGeneration(request.generationId());

        History history = historyRepository.save(History.builder()
                .generation(generation)
                .title(request.title())
                .thumbnailUrl(request.thumbnailUrl())
                .description(request.description())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .build());

        saveImages(history, request.imageUrls());

        return HistoryDetailResponse.of(history, request.imageUrls());
    }

    @Transactional
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public HistoryDetailResponse update(Long id, HistoryUpdateRequest request) {
        History history = getHistory(id);
        Generation generation = getGeneration(request.generationId());

        history.update(generation, request.title(), request.description(), request.startDate(), request.endDate());

        if (request.thumbnailUrl() != null) {
            if (!request.thumbnailUrl().equals(history.getThumbnailUrl())) {
                s3Uploader.deleteByUrl(history.getThumbnailUrl());
            }
            history.updateThumbnail(request.thumbnailUrl());
        }

        List<String> imageUrls;
        if (request.imageUrls() != null && !request.imageUrls().isEmpty()) {
            // 새 목록에 없는(=제거된) 이미지의 S3 파일만 삭제 - 그대로 유지되는 이미지의 URL을 지우면 안 되므로 차집합만 계산
            Set<String> newImageUrls = Set.copyOf(request.imageUrls());
            getImageUrls(history).stream()
                    .filter(url -> !newImageUrls.contains(url))
                    .forEach(s3Uploader::deleteByUrl);
            historyImageRepository.deleteAllByHistory(history);
            imageUrls = request.imageUrls();
            saveImages(history, imageUrls);
        } else {
            imageUrls = getImageUrls(history);
        }

        return HistoryDetailResponse.of(history, imageUrls);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public void delete(Long id) {
        History history = getHistory(id);
        s3Uploader.deleteByUrl(history.getThumbnailUrl());
        getImageUrls(history).forEach(s3Uploader::deleteByUrl);
        historyImageRepository.deleteAllByHistory(history);
        historyRepository.delete(history);
    }

    private void saveImages(History history, List<String> imageUrls) {
        for (String imageUrl : imageUrls) {
            historyImageRepository.save(HistoryImage.builder()
                    .history(history)
                    .imageUrl(imageUrl)
                    .build());
        }
    }

    private List<String> getImageUrls(History history) {
        return historyImageRepository.findByHistoryOrderByIdAsc(history).stream()
                .map(HistoryImage::getImageUrl)
                .toList();
    }

    private History getHistory(Long id) {
        return historyRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.HISTORY_NOT_FOUND, "존재하지 않는 게시물입니다. id=" + id));
    }

    private Generation getGeneration(Long generationId) {
        return generationRepository.findById(generationId)
                .orElseThrow(() -> new CustomException(ErrorCode.GENERATION_NOT_FOUND, "존재하지 않는 기수입니다. id=" + generationId));
    }
}

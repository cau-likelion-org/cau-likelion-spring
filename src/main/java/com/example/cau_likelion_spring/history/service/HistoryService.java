package com.example.cau_likelion_spring.history.service;

import com.example.cau_likelion_spring.history.domain.History;
import com.example.cau_likelion_spring.history.domain.HistoryImage;
import com.example.cau_likelion_spring.history.dto.HistoryCreateRequest;
import com.example.cau_likelion_spring.history.dto.HistoryDetailResponse;
import com.example.cau_likelion_spring.history.dto.HistoryListResponse;
import com.example.cau_likelion_spring.history.dto.HistoryUpdateRequest;
import com.example.cau_likelion_spring.history.repository.HistoryImageRepository;
import com.example.cau_likelion_spring.history.repository.HistoryRepository;
import com.example.cau_likelion_spring.organization.domain.Generation;
import com.example.cau_likelion_spring.organization.repository.GenerationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HistoryService {

    private final HistoryRepository historyRepository;
    private final HistoryImageRepository historyImageRepository;
    private final GenerationRepository generationRepository;

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

        historyImageRepository.deleteAllByHistory(history);
        saveImages(history, request.imageUrls());

        return HistoryDetailResponse.of(history, request.imageUrls());
    }

    @Transactional
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public void delete(Long id) {
        History history = getHistory(id);
        historyImageRepository.deleteAllByHistory(history);
        historyRepository.delete(history);
    }

    private void saveImages(History history, List<String> imageUrls) {
        for (int i = 0; i < imageUrls.size(); i++) {
            historyImageRepository.save(HistoryImage.builder()
                    .history(history)
                    .imageUrl(imageUrls.get(i))
                    .sortOrder(i)
                    .build());
        }
        history.updateThumbnail(imageUrls.get(0));
    }

    private List<String> getImageUrls(History history) {
        return historyImageRepository.findByHistoryOrderBySortOrderAsc(history).stream()
                .map(HistoryImage::getImageUrl)
                .toList();
    }

    private History getHistory(Long id) {
        return historyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 게시물입니다."));
    }

    private Generation getGeneration(Long generationId) {
        return generationRepository.findById(generationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 기수입니다."));
    }
}

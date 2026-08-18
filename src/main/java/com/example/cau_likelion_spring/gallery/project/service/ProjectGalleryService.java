package com.example.cau_likelion_spring.gallery.project.service;

import com.example.cau_likelion_spring.gallery.project.domain.ProjectGallery;
import com.example.cau_likelion_spring.gallery.project.domain.ProjectGalleryImage;
import com.example.cau_likelion_spring.gallery.project.dto.ProjectGalleryCreateRequest;
import com.example.cau_likelion_spring.gallery.project.dto.ProjectGalleryDetailResponse;
import com.example.cau_likelion_spring.gallery.project.dto.ProjectGalleryListResponse;
import com.example.cau_likelion_spring.gallery.project.dto.ProjectGalleryUpdateRequest;
import com.example.cau_likelion_spring.gallery.project.repository.ProjectGalleryImageRepository;
import com.example.cau_likelion_spring.gallery.project.repository.ProjectGalleryRepository;
import com.example.cau_likelion_spring.global.exception.CustomException;
import com.example.cau_likelion_spring.global.exception.ErrorCode;
import com.example.cau_likelion_spring.global.util.S3Uploader;
import com.example.cau_likelion_spring.organization.domain.Generation;
import com.example.cau_likelion_spring.organization.repository.GenerationRepository;
import com.example.cau_likelion_spring.project.domain.ProjectCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectGalleryService {

    private final ProjectGalleryRepository projectGalleryRepository;
    private final ProjectGalleryImageRepository projectGalleryImageRepository;
    private final GenerationRepository generationRepository;
    private final S3Uploader s3Uploader;

    public List<ProjectGalleryListResponse> getList(Integer generationNumber, ProjectCategory category) {
        List<ProjectGallery> projectGalleries;

        if (generationNumber != null && category != null) {
            projectGalleries = projectGalleryRepository.findByGeneration_NumberAndCategoryOrderByStartDateDesc(generationNumber, category);
        } else if (generationNumber != null) {
            projectGalleries = projectGalleryRepository.findByGeneration_NumberOrderByStartDateDesc(generationNumber);
        } else if (category != null) {
            projectGalleries = projectGalleryRepository.findByCategoryOrderByStartDateDesc(category);
        } else {
            projectGalleries = projectGalleryRepository.findAllByOrderByStartDateDesc();
        }

        return projectGalleries.stream()
                .map(ProjectGalleryListResponse::from)
                .toList();
    }

    public ProjectGalleryDetailResponse getDetail(Long id) {
        ProjectGallery projectGallery = getProjectGallery(id);
        List<String> imageUrls = getImageUrls(projectGallery);
        return ProjectGalleryDetailResponse.of(projectGallery, imageUrls);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN', 'PRESIDENT')")
    public ProjectGalleryDetailResponse create(ProjectGalleryCreateRequest request) {
        Generation generation = getGeneration(request.generationId());

        ProjectGallery projectGallery = projectGalleryRepository.save(ProjectGallery.builder()
                .generation(generation)
                .category(request.category())
                .title(request.title())
                .thumbnailUrl(request.thumbnailUrl())
                .description(request.description())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .build());

        saveImages(projectGallery, request.imageUrls());

        return ProjectGalleryDetailResponse.of(projectGallery, request.imageUrls());
    }

    @Transactional
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN', 'PRESIDENT')")
    public ProjectGalleryDetailResponse update(Long id, ProjectGalleryUpdateRequest request) {
        ProjectGallery projectGallery = getProjectGallery(id);
        Generation generation = getGeneration(request.generationId());

        projectGallery.update(generation, request.category(), request.title(), request.description(),
                request.startDate(), request.endDate());

        if (request.thumbnailUrl() != null) {
            if (!request.thumbnailUrl().equals(projectGallery.getThumbnailUrl())) {
                s3Uploader.deleteByUrl(projectGallery.getThumbnailUrl());
            }
            projectGallery.updateThumbnail(request.thumbnailUrl());
        }

        List<String> imageUrls;
        if (request.imageUrls() != null && !request.imageUrls().isEmpty()) {
            // 새 목록에 없는(=제거된) 이미지의 S3 파일만 삭제 - 그대로 유지되는 이미지의 URL을 지우면 안 되므로 차집합만 계산
            Set<String> newImageUrls = Set.copyOf(request.imageUrls());
            getImageUrls(projectGallery).stream()
                    .filter(url -> !newImageUrls.contains(url))
                    .forEach(s3Uploader::deleteByUrl);
            projectGalleryImageRepository.deleteAllByProjectGallery(projectGallery);
            imageUrls = request.imageUrls();
            saveImages(projectGallery, imageUrls);
        } else {
            imageUrls = getImageUrls(projectGallery);
        }

        return ProjectGalleryDetailResponse.of(projectGallery, imageUrls);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN', 'PRESIDENT')")
    public void delete(Long id) {
        ProjectGallery projectGallery = getProjectGallery(id);
        s3Uploader.deleteByUrl(projectGallery.getThumbnailUrl());
        getImageUrls(projectGallery).forEach(s3Uploader::deleteByUrl);
        projectGalleryImageRepository.deleteAllByProjectGallery(projectGallery);
        projectGalleryRepository.delete(projectGallery);
    }

    private void saveImages(ProjectGallery projectGallery, List<String> imageUrls) {
        for (String imageUrl : imageUrls) {
            projectGalleryImageRepository.save(ProjectGalleryImage.builder()
                    .projectGallery(projectGallery)
                    .imageUrl(imageUrl)
                    .build());
        }
    }

    private List<String> getImageUrls(ProjectGallery projectGallery) {
        return projectGalleryImageRepository.findByProjectGalleryOrderByIdAsc(projectGallery).stream()
                .map(ProjectGalleryImage::getImageUrl)
                .toList();
    }

    private ProjectGallery getProjectGallery(Long id) {
        return projectGalleryRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_GALLERY_NOT_FOUND, "존재하지 않는 게시물입니다. id=" + id));
    }

    private Generation getGeneration(Long generationId) {
        return generationRepository.findById(generationId)
                .orElseThrow(() -> new CustomException(ErrorCode.GENERATION_NOT_FOUND, "존재하지 않는 기수입니다. id=" + generationId));
    }
}

package com.example.cau_likelion_spring.project.service;

import com.example.cau_likelion_spring.organization.domain.Generation;
import com.example.cau_likelion_spring.global.exception.CustomException;
import com.example.cau_likelion_spring.global.exception.ErrorCode;
import com.example.cau_likelion_spring.organization.repository.GenerationRepository;
import com.example.cau_likelion_spring.project.domain.Project;
import com.example.cau_likelion_spring.project.domain.ProjectCategory;
import com.example.cau_likelion_spring.project.domain.ProjectImage;
import com.example.cau_likelion_spring.project.domain.ProjectLink;
import com.example.cau_likelion_spring.project.domain.ProjectMember;
import com.example.cau_likelion_spring.project.dto.ProjectRequest;
import com.example.cau_likelion_spring.project.dto.ProjectResponse;
import com.example.cau_likelion_spring.project.repository.ProjectImageRepository;
import com.example.cau_likelion_spring.project.repository.ProjectLinkRepository;
import com.example.cau_likelion_spring.project.repository.ProjectMemberRepository;
import com.example.cau_likelion_spring.project.repository.ProjectRepository;
import com.example.cau_likelion_spring.global.util.S3Uploader;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectImageRepository projectImageRepository;
    private final ProjectLinkRepository projectLinkRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final GenerationRepository generationRepository;
    private final S3Uploader s3Uploader;

    @Transactional
    public ProjectResponse create(ProjectRequest request) {
        Generation generation = getGeneration(request.generationId());

        Project project = projectRepository.save(Project.builder()
                .generation(generation)
                .title(request.title())
                .category(request.category())
                .stack(request.stack())
                .tagline(request.tagline())
                .summary(request.summary())
                .teamName(request.teamName())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .banner(request.banner())
                .build());

        saveChildren(project, request);

        return toResponse(project);
    }

    public List<ProjectResponse> getAll(Long generationId, ProjectCategory category) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");

        List<Project> projects;
        if (generationId != null && category != null) {
            projects = projectRepository.findByGeneration_IdAndCategory(generationId, category, sort);
        } else if (generationId != null) {
            projects = projectRepository.findByGeneration_Id(generationId, sort);
        } else if (category != null) {
            projects = projectRepository.findByCategory(category, sort);
        } else {
            projects = projectRepository.findAll(sort);
        }

        return projects.stream()
                .map(this::toResponse)
                .toList();
    }

    public ProjectResponse getById(Long id) {
        return toResponse(getProject(id));
    }

    @Transactional
    public ProjectResponse update(Long id, ProjectRequest request) {
        Project project = getProject(id);
        Generation generation = getGeneration(request.generationId());

        if (request.banner() != null && !request.banner().equals(project.getBanner())) {
            s3Uploader.deleteByUrl(project.getBanner());
        }

        project.update(generation, request.title(), request.category(), request.stack(), request.tagline(),
                request.summary(), request.teamName(), request.startDate(), request.endDate(), request.banner());

        // 새 목록에 없는(=제거된) 이미지의 S3 파일만 삭제 - 그대로 유지되는 이미지의 URL을 지우면 안 되므로 차집합만 계산
        Set<String> newImageUrls = nullToEmpty(request.images()).stream()
                .map(ProjectRequest.ImageRequest::imageUrl)
                .collect(Collectors.toSet());
        projectImageRepository.findAllByProject(project).stream()
                .map(ProjectImage::getImageUrl)
                .filter(url -> !newImageUrls.contains(url))
                .forEach(s3Uploader::deleteByUrl);
        projectImageRepository.deleteAllByProject(project);
        projectLinkRepository.deleteAllByProject(project);
        projectMemberRepository.deleteAllByProject(project);

        saveChildren(project, request);

        return toResponse(project);
    }

    @Transactional
    public void delete(Long id) {
        Project project = getProject(id);

        s3Uploader.deleteByUrl(project.getBanner());
        projectImageRepository.findAllByProject(project).forEach(image -> s3Uploader.deleteByUrl(image.getImageUrl()));
        projectImageRepository.deleteAllByProject(project);
        projectLinkRepository.deleteAllByProject(project);
        projectMemberRepository.deleteAllByProject(project);

        projectRepository.delete(project);
    }

    private void saveChildren(Project project, ProjectRequest request) {
        List<ProjectImage> images = nullToEmpty(request.images()).stream()
                .map(image -> ProjectImage.builder()
                        .project(project)
                        .imageUrl(image.imageUrl())
                        .isMain(image.isMain())
                        .displayOrder(image.displayOrder())
                        .build())
                .toList();
        projectImageRepository.saveAll(images);

        List<ProjectLink> links = nullToEmpty(request.links()).stream()
                .map(link -> ProjectLink.builder()
                        .project(project)
                        .platform(link.platform())
                        .url(link.url())
                        .build())
                .toList();
        projectLinkRepository.saveAll(links);

        List<ProjectMember> members = nullToEmpty(request.members()).stream()
                .map(member -> ProjectMember.builder()
                        .project(project)
                        .name(member.name())
                        .part(member.part())
                        .build())
                .toList();
        projectMemberRepository.saveAll(members);
    }

    private ProjectResponse toResponse(Project project) {
        List<ProjectImage> images = projectImageRepository.findAllByProject(project);
        List<ProjectLink> links = projectLinkRepository.findAllByProject(project);
        List<ProjectMember> members = projectMemberRepository.findAllByProject(project);
        return ProjectResponse.of(project, images, links, members);
    }

    private Project getProject(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND, "존재하지 않는 프로젝트입니다. id=" + id));
    }

    private Generation getGeneration(Long id) {
        return generationRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.GENERATION_NOT_FOUND, "존재하지 않는 기수입니다. id=" + id));
    }

    private static <T> List<T> nullToEmpty(List<T> list) {
        return list == null ? Collections.emptyList() : list;
    }
}

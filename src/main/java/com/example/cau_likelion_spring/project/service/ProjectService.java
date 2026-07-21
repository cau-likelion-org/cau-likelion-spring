package com.example.cau_likelion_spring.project.service;

import com.example.cau_likelion_spring.organization.domain.Generation;
import com.example.cau_likelion_spring.organization.repository.GenerationRepository;
import com.example.cau_likelion_spring.project.domain.Project;
import com.example.cau_likelion_spring.project.domain.ProjectImage;
import com.example.cau_likelion_spring.project.domain.ProjectLink;
import com.example.cau_likelion_spring.project.domain.ProjectMember;
import com.example.cau_likelion_spring.project.dto.ProjectRequest;
import com.example.cau_likelion_spring.project.dto.ProjectResponse;
import com.example.cau_likelion_spring.project.exception.GenerationNotFoundException;
import com.example.cau_likelion_spring.project.exception.ProjectNotFoundException;
import com.example.cau_likelion_spring.project.repository.ProjectImageRepository;
import com.example.cau_likelion_spring.project.repository.ProjectLinkRepository;
import com.example.cau_likelion_spring.project.repository.ProjectMemberRepository;
import com.example.cau_likelion_spring.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectImageRepository projectImageRepository;
    private final ProjectLinkRepository projectLinkRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final GenerationRepository generationRepository;

    @Transactional
    public ProjectResponse create(ProjectRequest request) {
        Generation generation = getGeneration(request.generationId());

        Project project = projectRepository.save(Project.builder()
                .generation(generation)
                .title(request.title())
                .category(request.category())
                .stack(request.stack())
                .summary(request.summary())
                .detail(request.detail())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .build());

        saveChildren(project, request);

        return toResponse(project);
    }

    public List<ProjectResponse> getAll() {
        return projectRepository.findAll().stream()
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

        project.update(generation, request.title(), request.category(), request.stack(),
                request.summary(), request.detail(), request.startDate(), request.endDate());

        projectImageRepository.deleteAllByProject(project);
        projectLinkRepository.deleteAllByProject(project);
        projectMemberRepository.deleteAllByProject(project);

        saveChildren(project, request);

        return toResponse(project);
    }

    @Transactional
    public void delete(Long id) {
        Project project = getProject(id);

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
                .orElseThrow(() -> new ProjectNotFoundException(id));
    }

    private Generation getGeneration(Long id) {
        return generationRepository.findById(id)
                .orElseThrow(() -> new GenerationNotFoundException(id));
    }

    private static <T> List<T> nullToEmpty(List<T> list) {
        return list == null ? Collections.emptyList() : list;
    }
}

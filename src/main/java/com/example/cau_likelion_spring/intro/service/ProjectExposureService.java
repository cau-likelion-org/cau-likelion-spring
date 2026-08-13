package com.example.cau_likelion_spring.intro.service;

import com.example.cau_likelion_spring.global.exception.CustomException;
import com.example.cau_likelion_spring.global.exception.ErrorCode;
import com.example.cau_likelion_spring.project.domain.Project;
import com.example.cau_likelion_spring.project.dto.ProjectResponse;
import com.example.cau_likelion_spring.project.repository.ProjectImageRepository;
import com.example.cau_likelion_spring.project.repository.ProjectLinkRepository;
import com.example.cau_likelion_spring.project.repository.ProjectMemberRepository;
import com.example.cau_likelion_spring.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 랜딩페이지에 노출할 프로젝트를 지정하는 로직.
 * 데이터(Project.isExposed)는 project 도메인이 소유하지만, "랜딩페이지에 뭘 띄울지"는
 * 정책적으로 intro(랜딩페이지) 도메인의 관심사라 서비스/API는 여기서 다룬다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectExposureService {

    private final ProjectRepository projectRepository;
    private final ProjectImageRepository projectImageRepository;
    private final ProjectLinkRepository projectLinkRepository;
    private final ProjectMemberRepository projectMemberRepository;

    @Transactional
    public List<ProjectResponse> updateExposure(List<Long> exposedProjectIds) {
        Set<Long> targetIds = Set.copyOf(exposedProjectIds);

        List<Project> allProjects = projectRepository.findAll();

        Set<Long> existingIds = allProjects.stream().map(Project::getId).collect(Collectors.toSet());
        Set<Long> notFoundIds = targetIds.stream()
                .filter(id -> !existingIds.contains(id))
                .collect(Collectors.toSet());
        if (!notFoundIds.isEmpty()) {
            throw new CustomException(ErrorCode.PROJECT_NOT_FOUND, "존재하지 않는 프로젝트입니다. id=" + notFoundIds);
        }

        allProjects.forEach(project -> project.updateExposure(targetIds.contains(project.getId())));

        return allProjects.stream()
                .filter(Project::getIsExposed)
                .map(this::toResponse)
                .toList();
    }

    private ProjectResponse toResponse(Project project) {
        return ProjectResponse.of(
                project,
                projectImageRepository.findAllByProject(project),
                projectLinkRepository.findAllByProject(project),
                projectMemberRepository.findAllByProject(project)
        );
    }
}

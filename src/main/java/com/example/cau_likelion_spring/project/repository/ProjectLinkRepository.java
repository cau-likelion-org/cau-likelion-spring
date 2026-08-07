package com.example.cau_likelion_spring.project.repository;

import com.example.cau_likelion_spring.project.domain.Project;
import com.example.cau_likelion_spring.project.domain.ProjectLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectLinkRepository extends JpaRepository<ProjectLink, Long> {

    List<ProjectLink> findAllByProject(Project project);

    void deleteAllByProject(Project project);
}

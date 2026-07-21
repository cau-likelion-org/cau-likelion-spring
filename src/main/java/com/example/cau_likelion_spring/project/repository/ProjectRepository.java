package com.example.cau_likelion_spring.project.repository;

import com.example.cau_likelion_spring.project.domain.Project;
import com.example.cau_likelion_spring.project.domain.ProjectCategory;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByGeneration_IdAndCategory(Long generationId, ProjectCategory category, Sort sort);

    List<Project> findByGeneration_Id(Long generationId, Sort sort);

    List<Project> findByCategory(ProjectCategory category, Sort sort);
}

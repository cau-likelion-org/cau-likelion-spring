package com.example.cau_likelion_spring.gallery.project.repository;

import com.example.cau_likelion_spring.gallery.project.domain.ProjectGallery;
import com.example.cau_likelion_spring.project.domain.ProjectCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectGalleryRepository extends JpaRepository<ProjectGallery, Long> {

    List<ProjectGallery> findAllByOrderByStartDateDesc();

    List<ProjectGallery> findByGeneration_NumberOrderByStartDateDesc(Integer generationNumber);

    List<ProjectGallery> findByCategoryOrderByStartDateDesc(ProjectCategory category);

    List<ProjectGallery> findByGeneration_NumberAndCategoryOrderByStartDateDesc(Integer generationNumber, ProjectCategory category);
}

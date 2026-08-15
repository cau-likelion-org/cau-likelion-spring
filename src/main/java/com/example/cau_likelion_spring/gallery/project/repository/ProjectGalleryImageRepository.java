package com.example.cau_likelion_spring.gallery.project.repository;

import com.example.cau_likelion_spring.gallery.project.domain.ProjectGallery;
import com.example.cau_likelion_spring.gallery.project.domain.ProjectGalleryImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectGalleryImageRepository extends JpaRepository<ProjectGalleryImage, Long> {

    List<ProjectGalleryImage> findByProjectGalleryOrderByIdAsc(ProjectGallery projectGallery);

    void deleteAllByProjectGallery(ProjectGallery projectGallery);
}

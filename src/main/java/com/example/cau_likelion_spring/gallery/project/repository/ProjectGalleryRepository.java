package com.example.cau_likelion_spring.gallery.project.repository;

import com.example.cau_likelion_spring.gallery.project.domain.ProjectGallery;
import com.example.cau_likelion_spring.project.domain.ProjectCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectGalleryRepository extends JpaRepository<ProjectGallery, Long> {

    // 전체 조회 (필터 없음): 기수 내림차순 -> 시작일 내림차순 -> 이름 오름차순
    List<ProjectGallery> findAllByOrderByGeneration_NumberDescStartDateDescTitleAsc();

    // 기수로만 필터링된 조회: 기수는 이미 고정이라 정렬에서 제외, 시작일 내림차순 -> 이름 오름차순
    List<ProjectGallery> findByGeneration_NumberOrderByStartDateDescTitleAsc(Integer generationNumber);

    // 프로젝트 구분으로만 필터링된 조회: 기수 내림차순 -> 시작일 내림차순 -> 이름 오름차순
    List<ProjectGallery> findByCategoryOrderByGeneration_NumberDescStartDateDescTitleAsc(ProjectCategory category);

    // 기수 + 프로젝트 구분으로 필터링된 조회: 기수는 이미 고정이라 정렬에서 제외, 시작일 내림차순 -> 이름 오름차순
    List<ProjectGallery> findByGeneration_NumberAndCategoryOrderByStartDateDescTitleAsc(Integer generationNumber, ProjectCategory category);
}

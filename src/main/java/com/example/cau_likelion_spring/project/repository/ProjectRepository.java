package com.example.cau_likelion_spring.project.repository;

import com.example.cau_likelion_spring.project.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
}

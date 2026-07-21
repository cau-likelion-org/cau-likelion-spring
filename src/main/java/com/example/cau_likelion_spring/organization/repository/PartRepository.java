package com.example.cau_likelion_spring.organization.repository;

import com.example.cau_likelion_spring.organization.domain.Part;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartRepository extends JpaRepository<Part, Long> {
}

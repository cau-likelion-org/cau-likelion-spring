package com.example.cau_likelion_spring.organization.repository;

import com.example.cau_likelion_spring.organization.domain.Part;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PartRepository extends JpaRepository<Part, Long> {

    Optional<Part> findByNameAndGeneration_Number(String name, Integer number);

    List<Part> findAllByGeneration_IdIn(List<Long> generationIds);

    List<Part> findAllByGeneration_Id(Long generationId);
}

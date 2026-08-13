package com.example.cau_likelion_spring.intro.repository;

import com.example.cau_likelion_spring.intro.domain.Roadmap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoadmapRepository extends JpaRepository<Roadmap, Long> {

    // 가장 최근에 추가된(=id가 가장 큰) 로드맵 이미지 하나를 조회한다
    Optional<Roadmap> findTopByOrderByIdDesc();
}

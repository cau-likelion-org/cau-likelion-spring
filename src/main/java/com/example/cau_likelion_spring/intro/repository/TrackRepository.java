package com.example.cau_likelion_spring.intro.repository;

import com.example.cau_likelion_spring.intro.domain.Track;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrackRepository extends JpaRepository<Track, Long> {
}

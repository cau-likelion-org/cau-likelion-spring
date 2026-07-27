package com.example.cau_likelion_spring.intro.repository;

import com.example.cau_likelion_spring.intro.domain.Activity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
}

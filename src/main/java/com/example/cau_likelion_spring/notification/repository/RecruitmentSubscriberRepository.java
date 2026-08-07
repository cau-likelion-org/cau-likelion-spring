package com.example.cau_likelion_spring.notification.repository;

import com.example.cau_likelion_spring.notification.domain.RecruitmentSubscriber;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecruitmentSubscriberRepository extends JpaRepository<RecruitmentSubscriber, Long> {

    boolean existsByEmail(String email);
}

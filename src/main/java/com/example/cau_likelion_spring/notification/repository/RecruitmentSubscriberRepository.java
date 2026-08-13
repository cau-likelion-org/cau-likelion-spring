package com.example.cau_likelion_spring.notification.repository;

import com.example.cau_likelion_spring.notification.domain.RecruitmentSubscriber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RecruitmentSubscriberRepository extends JpaRepository<RecruitmentSubscriber, Long> {

    boolean existsByEmail(String email);

    List<RecruitmentSubscriber> findAllByInterestParts_NameOrderByRegisteredAtDesc(String interestPartName);

    @Query("SELECT DISTINCT p.name FROM RecruitmentSubscriber s JOIN s.interestParts p ORDER BY p.name")
    List<String> findDistinctInterestPartNames();
}

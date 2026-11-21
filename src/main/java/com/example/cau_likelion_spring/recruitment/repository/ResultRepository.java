package com.example.cau_likelion_spring.recruitment.repository;

import com.example.cau_likelion_spring.recruitment.domain.Application;
import com.example.cau_likelion_spring.recruitment.domain.Result;
import com.example.cau_likelion_spring.recruitment.domain.Round;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ResultRepository extends JpaRepository<Result, Long> {

    /** 해당 지원서들의 결과 (결과는 지원서당 하나) */
    List<Result> findAllByApplicationIn(Collection<Application> applications);

    Optional<Result> findByApplication(Application application);

    /** 해당 회차에서 같은 확정 면접 일시에 배정된 다른 지원서 수 (본인 제외) */
    long countByFixedInterviewDateAndApplication_RoundPart_RoundAndApplicationNot(
            LocalDateTime fixedInterviewDate, Round round, Application application);
}

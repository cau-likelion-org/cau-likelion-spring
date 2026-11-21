package com.example.cau_likelion_spring.recruitment.repository;

import com.example.cau_likelion_spring.recruitment.domain.Application;
import com.example.cau_likelion_spring.recruitment.domain.Result;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ResultRepository extends JpaRepository<Result, Long> {

    /** 해당 지원서들의 결과 (결과는 지원서당 하나) */
    List<Result> findAllByApplicationIn(Collection<Application> applications);

    Optional<Result> findByApplication(Application application);
}

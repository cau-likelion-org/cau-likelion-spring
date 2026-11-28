package com.example.cau_likelion_spring.recruitment.repository;

import com.example.cau_likelion_spring.recruitment.domain.Application;
import com.example.cau_likelion_spring.recruitment.domain.ApplicationResult;
import com.example.cau_likelion_spring.recruitment.domain.Result;
import com.example.cau_likelion_spring.recruitment.domain.Round;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ResultRepository extends JpaRepository<Result, Long> {

    /** 해당 지원서들의 결과 (결과는 지원서당 하나) */
    List<Result> findAllByApplicationIn(Collection<Application> applications);

    Optional<Result> findByApplication(Application application);

    /** 해당 회차에서 서류 결과가 입력된(보류 포함) 지원서 수 */
    @Query("select count(r) from Result r where r.application.roundPart.round = :round and r.documentResult is not null")
    long countDocumentResultEntered(@Param("round") Round round);

    /** 해당 회차에서 서류 결과가 documentResult인 지원서 수 */
    @Query("select count(r) from Result r where r.application.roundPart.round = :round and r.documentResult = :documentResult")
    long countByDocumentResult(@Param("round") Round round, @Param("documentResult") ApplicationResult documentResult);

    /** 해당 회차에서 서류 결과가 documentResult이고 확정 면접 일시·장소가 모두 입력된 지원서 수 */
    @Query("select count(r) from Result r where r.application.roundPart.round = :round and r.documentResult = :documentResult "
            + "and r.fixedInterviewDate is not null and r.fixedInterviewSpot is not null")
    long countInterviewScheduleEntered(@Param("round") Round round, @Param("documentResult") ApplicationResult documentResult);

    /** 해당 회차에서 서류 결과가 documentResult이고 면접(최종) 결과가 입력된(보류 포함) 지원서 수 */
    @Query("select count(r) from Result r where r.application.roundPart.round = :round and r.documentResult = :documentResult "
            + "and r.finalResult is not null")
    long countFinalResultEntered(@Param("round") Round round, @Param("documentResult") ApplicationResult documentResult);

    /** 해당 회차에서 같은 확정 면접 일시에 배정된 다른 지원서 수 (본인 제외) */
    @Query("select count(r) from Result r where r.application.roundPart.round = :round "
            + "and r.fixedInterviewDate = :fixedInterviewDate and r.application <> :application")
    long countOthersAtSameInterviewDate(@Param("round") Round round, @Param("application") Application application,
                                        @Param("fixedInterviewDate") LocalDateTime fixedInterviewDate);
}

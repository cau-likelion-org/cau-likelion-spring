package com.example.cau_likelion_spring.recruitment.repository;

import com.example.cau_likelion_spring.recruitment.domain.Application;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 리크루팅 대시보드(7.9.1) 집계 쿼리.
 * 엔티티를 불러와 자바에서 세지 않고, DB에서 COUNT·GROUP BY로 숫자만 받아온다.
 * 회차 기준은 지원서 → 지원 파트(round_part) → 회차(round_id)다.
 */
public interface DashboardStatsRepository extends Repository<Application, Long> {

    /** 본인 확인 인원 (회차에 가입한 지원자 계정 수) */
    @Query(value = "SELECT COUNT(*) FROM recruitment_applicant WHERE round_id = :roundId", nativeQuery = true)
    long countIdentified(@Param("roundId") Long roundId);

    /** 서류 제출 인원 */
    @Query(value = """
            SELECT COUNT(*)
            FROM recruitment_application a
            JOIN recruitment_round_part rp ON a.round_part_id = rp.id
            WHERE rp.round_id = :roundId
            """, nativeQuery = true)
    long countSubmitted(@Param("roundId") Long roundId);

    /** 지각 제출 인원 (마감 이후 제출) */
    @Query(value = """
            SELECT COUNT(*)
            FROM recruitment_application a
            JOIN recruitment_round_part rp ON a.round_part_id = rp.id
            WHERE rp.round_id = :roundId AND a.submit_at > :deadLine
            """, nativeQuery = true)
    long countLate(@Param("roundId") Long roundId, @Param("deadLine") LocalDateTime deadLine);

    /** 전체 모집 정원 (파트별 정원 합) */
    @Query(value = """
            SELECT CAST(COALESCE(SUM(head_count), 0) AS SIGNED)
            FROM recruitment_round_part
            WHERE round_id = :roundId
            """, nativeQuery = true)
    long sumHeadCount(@Param("roundId") Long roundId);

    /** 서류 결과별 인원 (PASS, FAIL 등) */
    @Query(value = """
            SELECT COUNT(*)
            FROM recruitment_result r
            JOIN recruitment_application a ON r.application_id = a.id
            JOIN recruitment_round_part rp ON a.round_part_id = rp.id
            WHERE rp.round_id = :roundId AND r.document_result = :result
            """, nativeQuery = true)
    long countDocumentResult(@Param("roundId") Long roundId, @Param("result") String result);

    /** 면접(최종) 결과별 인원 (PASS, FAIL 등) */
    @Query(value = """
            SELECT COUNT(*)
            FROM recruitment_result r
            JOIN recruitment_application a ON r.application_id = a.id
            JOIN recruitment_round_part rp ON a.round_part_id = rp.id
            WHERE rp.round_id = :roundId AND r.final_result = :result
            """, nativeQuery = true)
    long countFinalResult(@Param("roundId") Long roundId, @Param("result") String result);

    /** 날짜별 제출 수 (제출이 있는 날짜만) */
    @Query(value = """
            SELECT DATE_FORMAT(a.submit_at, '%Y-%m-%d') AS submitDate, COUNT(*) AS cnt
            FROM recruitment_application a
            JOIN recruitment_round_part rp ON a.round_part_id = rp.id
            WHERE rp.round_id = :roundId AND a.submit_at IS NOT NULL
            GROUP BY DATE_FORMAT(a.submit_at, '%Y-%m-%d')
            """, nativeQuery = true)
    List<DateCount> countByDate(@Param("roundId") Long roundId);

    /** 파트 + 학과별 인원 (학과는 앞뒤 공백 제거) */
    @Query(value = """
            SELECT rp.part AS part, TRIM(a.department) AS department, COUNT(*) AS cnt
            FROM recruitment_application a
            JOIN recruitment_round_part rp ON a.round_part_id = rp.id
            WHERE rp.round_id = :roundId AND a.department IS NOT NULL AND TRIM(a.department) <> ''
            GROUP BY rp.part, TRIM(a.department)
            """, nativeQuery = true)
    List<PartDepartmentCount> countByPartAndDepartment(@Param("roundId") Long roundId);

    /** 학년별 인원 (있는 값만) */
    @Query(value = """
            SELECT a.grade AS val, COUNT(*) AS cnt
            FROM recruitment_application a
            JOIN recruitment_round_part rp ON a.round_part_id = rp.id
            WHERE rp.round_id = :roundId AND a.grade IS NOT NULL
            GROUP BY a.grade
            """, nativeQuery = true)
    List<ValueCount> countByGrade(@Param("roundId") Long roundId);

    /** 학적별 인원 (있는 값만) */
    @Query(value = """
            SELECT a.enrollment AS val, COUNT(*) AS cnt
            FROM recruitment_application a
            JOIN recruitment_round_part rp ON a.round_part_id = rp.id
            WHERE rp.round_id = :roundId AND a.enrollment IS NOT NULL
            GROUP BY a.enrollment
            """, nativeQuery = true)
    List<ValueCount> countByEnrollment(@Param("roundId") Long roundId);

    /** 지원 경로별 인원 (있는 값만) */
    @Query(value = """
            SELECT a.source AS val, COUNT(*) AS cnt
            FROM recruitment_application a
            JOIN recruitment_round_part rp ON a.round_part_id = rp.id
            WHERE rp.round_id = :roundId AND a.source IS NOT NULL
            GROUP BY a.source
            """, nativeQuery = true)
    List<ValueCount> countBySource(@Param("roundId") Long roundId);

    interface DateCount {
        String getSubmitDate();

        Long getCnt();
    }

    interface PartDepartmentCount {
        String getPart();

        String getDepartment();

        Long getCnt();
    }

    interface ValueCount {
        String getVal();

        Long getCnt();
    }
}

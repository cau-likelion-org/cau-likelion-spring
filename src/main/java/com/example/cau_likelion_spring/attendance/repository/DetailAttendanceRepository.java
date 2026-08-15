package com.example.cau_likelion_spring.attendance.repository;

import com.example.cau_likelion_spring.attendance.domain.AttendanceStatus;
import com.example.cau_likelion_spring.attendance.domain.DetailAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DetailAttendanceRepository extends JpaRepository<DetailAttendance, Long> {

    List<DetailAttendance> findByMember_IdOrderByWeeklyAttendance_WeekNumberAsc(Long memberId);

    void deleteAllByMember_Id(Long memberId);

    List<DetailAttendance> findByMember_IdInOrderByWeeklyAttendance_WeekNumberAsc(List<Long> memberIds);

    Optional<DetailAttendance> findByMember_IdAndWeeklyAttendance_Id(Long memberId, Long weeklyAttendanceId);

    List<DetailAttendance> findByStatusAndWeeklyAttendance_DateLessThanEqual(AttendanceStatus status, LocalDate date);

    /** 벌점표 계산용 - 여러 회원의 상태별 출결 횟수를 한 번에 집계 (마이페이지) */
    @Query("SELECT d.member.id AS memberId, d.status AS status, COUNT(d) AS count "
            + "FROM DetailAttendance d WHERE d.member.id IN :memberIds GROUP BY d.member.id, d.status")
    List<MemberStatusCount> countByStatusForMembers(@Param("memberIds") List<Long> memberIds);

    interface MemberStatusCount {
        Long getMemberId();

        AttendanceStatus getStatus();

        Long getCount();
    }
}

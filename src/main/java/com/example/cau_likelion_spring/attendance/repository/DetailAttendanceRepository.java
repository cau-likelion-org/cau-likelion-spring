package com.example.cau_likelion_spring.attendance.repository;

import com.example.cau_likelion_spring.attendance.domain.AttendanceStatus;
import com.example.cau_likelion_spring.attendance.domain.DetailAttendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DetailAttendanceRepository extends JpaRepository<DetailAttendance, Long> {

    List<DetailAttendance> findByMember_IdOrderByWeeklyAttendance_WeekNumberAsc(Long memberId);

    List<DetailAttendance> findByMember_IdInOrderByWeeklyAttendance_WeekNumberAsc(List<Long> memberIds);

    Optional<DetailAttendance> findByMember_IdAndWeeklyAttendance_Id(Long memberId, Long weeklyAttendanceId);

    List<DetailAttendance> findByStatusAndWeeklyAttendance_DateLessThanEqual(AttendanceStatus status, LocalDate date);
}

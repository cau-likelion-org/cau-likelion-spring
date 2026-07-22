package com.example.cau_likelion_spring.attendance.repository;

import com.example.cau_likelion_spring.attendance.domain.DetailAttendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DetailAttendanceRepository extends JpaRepository<DetailAttendance, Long> {

    List<DetailAttendance> findByMember_IdOrderByWeeklyAttendance_WeekNumberAsc(Long memberId);
}

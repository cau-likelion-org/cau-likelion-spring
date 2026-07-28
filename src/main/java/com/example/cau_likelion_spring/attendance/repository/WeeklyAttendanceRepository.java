package com.example.cau_likelion_spring.attendance.repository;

import com.example.cau_likelion_spring.attendance.domain.WeeklyAttendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface WeeklyAttendanceRepository extends JpaRepository<WeeklyAttendance, Long> {

    boolean existsByDate(LocalDate date);
}

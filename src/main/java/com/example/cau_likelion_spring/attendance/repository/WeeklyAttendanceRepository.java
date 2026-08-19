package com.example.cau_likelion_spring.attendance.repository;

import com.example.cau_likelion_spring.attendance.domain.WeeklyAttendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WeeklyAttendanceRepository extends JpaRepository<WeeklyAttendance, Long> {

    boolean existsByDate(LocalDate date);

    Optional<WeeklyAttendance> findByDate(LocalDate date);

    List<WeeklyAttendance> findAllByOrderByDateDesc();
}

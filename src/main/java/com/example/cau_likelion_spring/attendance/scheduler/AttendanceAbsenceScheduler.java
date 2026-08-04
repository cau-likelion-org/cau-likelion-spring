package com.example.cau_likelion_spring.attendance.scheduler;

import com.example.cau_likelion_spring.attendance.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AttendanceAbsenceScheduler {

    private final AttendanceService attendanceService;

    @Scheduled(cron = "${attendance.absence-check-cron}")
    public void checkUnauthorizedAbsences() {
        attendanceService.markUnauthorizedAbsences();
    }
}

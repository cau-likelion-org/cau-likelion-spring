package com.example.cau_likelion_spring.attendance.domain;

import com.example.cau_likelion_spring.global.common.BaseTimeEntity;
import com.example.cau_likelion_spring.member.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 사용자별 출석 체크 결과(사유)
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DetailAttendance extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "weekly_attendance_id", nullable = false)
    private WeeklyAttendance weeklyAttendance;

    @Enumerated(EnumType.STRING)
    private AttendanceStatus status;

    @Column(length = 255)
    private String detailReason;

    private LocalDateTime checkedAt;

    @Builder
    public DetailAttendance(Member member, WeeklyAttendance weeklyAttendance, AttendanceStatus status,
                             String detailReason, LocalDateTime checkedAt) {
        this.member = member;
        this.weeklyAttendance = weeklyAttendance;
        this.status = status;
        this.detailReason = detailReason;
        this.checkedAt = checkedAt;
    }

    public void checkIn(AttendanceStatus status, LocalDateTime checkedAt) {
        this.status = status;
        this.checkedAt = checkedAt;
    }

    public void markAsUnauthorizedAbsent() {
        this.status = AttendanceStatus.UNAUTHORIZED_ABSENT;
    }
}

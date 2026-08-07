package com.example.cau_likelion_spring.attendance.domain;

import com.example.cau_likelion_spring.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 출석체크 이벤트 (특정 날짜의 출석 체크)
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WeeklyAttendance extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;

    /** 출석 체크용 비밀번호 (4자리 숫자, 앞자리 0 보존을 위해 문자열로 저장) */
    @Column(length = 4, nullable = false)
    private String password;

    /** 주차 */
    private Integer weekNumber;

    @Builder
    public WeeklyAttendance(LocalDate date, String password, Integer weekNumber) {
        this.date = date;
        this.password = password;
        this.weekNumber = weekNumber;
    }
}

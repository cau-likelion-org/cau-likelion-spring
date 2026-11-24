package com.example.cau_likelion_spring.recruitment.domain;

import com.example.cau_likelion_spring.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "recruitment_interview_time")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InterviewTime extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "round_id")
    private Round round;

    private LocalDateTime interviewDate;

    @Builder
    public InterviewTime(Round round, LocalDateTime interviewDate) {
        this.round = round;
        this.interviewDate = interviewDate;
    }
}

package com.example.cau_likelion_spring.recruitment.domain;

import com.example.cau_likelion_spring.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 모집 회차.
 * "round"는 MySQL의 ROUND() 함수명과 같아 헷갈리고 의미도 일반적이라,
 * recruitment_ 접두어를 붙인 테이블명을 명시한다.
 */
@Entity
@Table(name = "recruitment_round")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Round extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    private String name;

    private LocalDateTime startDate;

    private LocalDateTime deadLine;

    private LocalDateTime interviewStartDate;

    private LocalDateTime interviewDeadLine;

    private LocalDateTime resultDate;

    private LocalDateTime otDate;

    @Builder
    public Round(String name, LocalDateTime startDate, LocalDateTime deadLine,
                 LocalDateTime interviewStartDate, LocalDateTime interviewDeadLine,
                 LocalDateTime resultDate, LocalDateTime otDate) {
        this.name = name;
        this.startDate = startDate;
        this.deadLine = deadLine;
        this.interviewStartDate = interviewStartDate;
        this.interviewDeadLine = interviewDeadLine;
        this.resultDate = resultDate;
        this.otDate = otDate;
    }
}

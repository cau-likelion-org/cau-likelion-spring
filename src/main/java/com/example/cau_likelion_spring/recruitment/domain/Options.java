package com.example.cau_likelion_spring.recruitment.domain;

import com.example.cau_likelion_spring.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 면접 선택지 항목.
 * "options"는 의미가 너무 일반적이고 InterviewOption과도 이름이 헷갈려,
 * recruitment_ 접두어를 붙인 테이블명을 명시한다.
 */
@Entity
@Table(name = "recruitment_options")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Options extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interview_option_id")
    private InterviewOption interviewOption;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interview_question_id")
    private InterviewQuestion interviewQuestion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "round_part_id")
    private RoundPart roundPart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "round_id")
    private Round round;

    @Column(length = 100)
    private String field;

    @Builder
    public Options(InterviewOption interviewOption, InterviewQuestion interviewQuestion,
                   RoundPart roundPart, Round round, String field) {
        this.interviewOption = interviewOption;
        this.interviewQuestion = interviewQuestion;
        this.roundPart = roundPart;
        this.round = round;
        this.field = field;
    }
}

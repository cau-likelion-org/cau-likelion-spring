package com.example.cau_likelion_spring.recruitment.domain;

import com.example.cau_likelion_spring.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "recruitment_interview_option")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InterviewOption extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interview_question_id")
    private InterviewQuestion interviewQuestion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "round_part_id")
    private RoundPart roundPart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "round_id")
    private Round round;

    @Column(length = 1000)
    private String question;

    @Column(length = 1000)
    private String detail;

    @Builder
    public InterviewOption(InterviewQuestion interviewQuestion, RoundPart roundPart, Round round,
                           String question, String detail) {
        this.interviewQuestion = interviewQuestion;
        this.roundPart = roundPart;
        this.round = round;
        this.question = question;
        this.detail = detail;
    }
}

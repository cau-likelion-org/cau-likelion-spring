package com.example.cau_likelion_spring.recruitment.domain;

import com.example.cau_likelion_spring.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "recruitment_interview_script")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InterviewScript extends BaseTimeEntity {

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

    /**
     * 답변 글자 수 제한.
     * "limit"은 SQL 예약어라 컬럼명으로 그대로 쓰면 DDL/쿼리가 깨지므로 char_limit으로 지정한다.
     */
    @Column(name = "char_limit")
    private Integer limit;

    @Builder
    public InterviewScript(InterviewQuestion interviewQuestion, RoundPart roundPart, Round round,
                           String question, String detail, Integer limit) {
        this.interviewQuestion = interviewQuestion;
        this.roundPart = roundPart;
        this.round = round;
        this.question = question;
        this.detail = detail;
        this.limit = limit;
    }
}

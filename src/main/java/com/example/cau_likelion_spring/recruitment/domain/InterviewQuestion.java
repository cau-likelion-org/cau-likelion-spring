package com.example.cau_likelion_spring.recruitment.domain;

import com.example.cau_likelion_spring.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 2단계 문항 자리.
 * 질문·설명 등 내용은 종류에 따라 InterviewScript(장문·링크·파일) 또는
 * InterviewOption + Options(객관식)에 담는다.
 */
@Entity
@Table(name = "recruitment_interview_question")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InterviewQuestion extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "round_part_id")
    private RoundPart roundPart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "round_id")
    private Round round;

    /** 문항 종류 */
    @Enumerated(EnumType.STRING)
    private QuestionType type;

    /** 필수 여부 */
    private Boolean required;

    /** 노출 순서 (오름차순) */
    private Integer sequence;

    @Builder
    public InterviewQuestion(RoundPart roundPart, Round round, QuestionType type,
                             Boolean required, Integer sequence) {
        this.roundPart = roundPart;
        this.round = round;
        this.type = type;
        this.required = required;
        this.sequence = sequence;
    }

    /** 문항 유형·필수 여부 수정 (내용은 InterviewScript/InterviewOption에서 따로 교체) */
    public void update(QuestionType type, Boolean required) {
        this.type = type;
        this.required = required;
    }

    public void changeSequence(Integer sequence) {
        this.sequence = sequence;
    }
}

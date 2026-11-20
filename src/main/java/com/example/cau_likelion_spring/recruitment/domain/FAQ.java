package com.example.cau_likelion_spring.recruitment.domain;

import com.example.cau_likelion_spring.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "recruitment_faq")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FAQ extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "round_id")
    private Round round;

    @Column(length = 1000)
    private String question;

    @Column(length = 1000)
    private String answer;

    @Builder
    public FAQ(Round round, String question, String answer) {
        this.round = round;
        this.question = question;
        this.answer = answer;
    }
}

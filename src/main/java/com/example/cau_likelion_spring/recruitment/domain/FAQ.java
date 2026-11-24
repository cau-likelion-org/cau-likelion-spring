package com.example.cau_likelion_spring.recruitment.domain;

import com.example.cau_likelion_spring.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 모집공고(회차)에 달리는 FAQ - 질문·답변 한 쌍.
 * 관리자 화면에서 일정과 함께 목록 통째로 저장하므로, 저장할 때마다 회차의 FAQ를 모두 지우고 새로 만든다 (RoundNoticeService).
 */
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

    /** 노출 순서 (1부터, 오름차순) */
    private Integer displayOrder;

    @Builder
    public FAQ(Round round, String question, String answer, Integer displayOrder) {
        this.round = round;
        this.question = question;
        this.answer = answer;
        this.displayOrder = displayOrder;
    }
}

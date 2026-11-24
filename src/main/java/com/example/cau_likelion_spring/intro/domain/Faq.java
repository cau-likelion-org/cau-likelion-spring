package com.example.cau_likelion_spring.intro.domain;

import com.example.cau_likelion_spring.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 랜딩 페이지 FAQ (질문 및 답변)
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Faq extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 200, nullable = false)
    private String question;

    @Column(length = 1000, nullable = false)
    private String answer;

    /**
     * 노출 순서 (오름차순).
     * 컬럼 추가 전에 만든 FAQ는 null일 수 있어 nullable로 두고, 조회 시 null은 맨 뒤(id 순)로 보낸다.
     */
    private Integer displayOrder;

    @Builder
    public Faq(String question, String answer, Integer displayOrder) {
        this.question = question;
        this.answer = answer;
        this.displayOrder = displayOrder;
    }

    public void update(String question, String answer) {
        this.question = question;
        this.answer = answer;
    }

    public void changeDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
}

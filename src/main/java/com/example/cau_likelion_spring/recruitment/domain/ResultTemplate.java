package com.example.cau_likelion_spring.recruitment.domain;

import com.example.cau_likelion_spring.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 결과 안내 문구 템플릿 (회차별 종류당 하나).
 * 저장 전에는 행이 없고 ResultTemplateType.defaultText를 기본 문구로 쓴다. 처음 저장할 때 만들고 이후에는 덮어쓴다.
 */
@Entity
@Table(name = "recruitment_result_template",
        uniqueConstraints = @UniqueConstraint(name = "uk_result_template_round_type", columnNames = {"round_id", "type"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ResultTemplate extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "round_id")
    private Round round;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private ResultTemplateType type;

    @Column(columnDefinition = "TEXT")
    private String text;

    @Builder
    public ResultTemplate(Round round, ResultTemplateType type, String text) {
        this.round = round;
        this.type = type;
        this.text = text;
    }

    /** 문구 수정 (덮어쓰기) */
    public void changeText(String text) {
        this.text = text;
    }
}

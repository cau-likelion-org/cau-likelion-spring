package com.example.cau_likelion_spring.recruitment.domain;

import com.example.cau_likelion_spring.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "recruitment_result_template")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ResultTemplate extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ResultTemplateType type;

    @Column(columnDefinition = "TEXT")
    private String text;

    @Builder
    public ResultTemplate(ResultTemplateType type, String text) {
        this.type = type;
        this.text = text;
    }
}

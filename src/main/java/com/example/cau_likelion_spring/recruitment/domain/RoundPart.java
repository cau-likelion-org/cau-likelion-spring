package com.example.cau_likelion_spring.recruitment.domain;

import com.example.cau_likelion_spring.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "recruitment_round_part")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoundPart extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "round_id")
    private Round round;

    @Column(length = 50)
    private String part;

    private Integer headCount;

    @Builder
    public RoundPart(Round round, String part, Integer headCount) {
        this.round = round;
        this.part = part;
        this.headCount = headCount;
    }

    public void changeHeadCount(Integer headCount) {
        this.headCount = headCount;
    }
}

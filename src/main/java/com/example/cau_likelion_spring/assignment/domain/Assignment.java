package com.example.cau_likelion_spring.assignment.domain;

import com.example.cau_likelion_spring.global.common.BaseTimeEntity;
import com.example.cau_likelion_spring.organization.domain.Part;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Assignment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "part_id", nullable = false)
    private Part part;

    /** 주차 */
    private Integer week;

    @Column(nullable = false)
    private String title;

    @Lob
    private String detail;

    private LocalDateTime endDate;

    @Builder
    public Assignment(Part part, Integer week, String title, String detail, LocalDateTime endDate) {
        this.part = part;
        this.week = week;
        this.title = title;
        this.detail = detail;
        this.endDate = endDate;
    }
}

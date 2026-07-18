package com.example.cau_likelion_spring.session.domain;

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
public class Session extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "part_id", nullable = false)
    private Part part;

    @Column(nullable = false)
    private String title;

    @Lob
    private String description;

    private LocalDateTime sessionDate;

    /** 세션 차수 */
    private Integer degree;

    private String thumbnailUrl;

    @Builder
    public Session(Part part, String title, String description, LocalDateTime sessionDate,
                   Integer degree, String thumbnailUrl) {
        this.part = part;
        this.title = title;
        this.description = description;
        this.sessionDate = sessionDate;
        this.degree = degree;
        this.thumbnailUrl = thumbnailUrl;
    }
}

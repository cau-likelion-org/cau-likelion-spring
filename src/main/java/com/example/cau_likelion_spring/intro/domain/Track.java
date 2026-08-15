package com.example.cau_likelion_spring.intro.domain;

import com.example.cau_likelion_spring.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 트랙(파트) 소개 - 랜딩/소개 페이지 노출용
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Track extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 10, nullable = false)
    private String koName;

    @Column(length = 50, nullable = false)
    private String enName;

    @Lob
    private String introduction;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "track_tech_stack", joinColumns = @JoinColumn(name = "track_id"))
    @Column(name = "tech_stack", length = 10)
    private List<String> techStack = new ArrayList<>();

    @Builder
    public Track(String koName, String enName, String introduction, List<String> techStack) {
        this.koName = koName;
        this.enName = enName;
        this.introduction = introduction;
        this.techStack = (techStack != null) ? techStack : new ArrayList<>();
    }

    public void update(String koName, String enName, String introduction, List<String> techStack) {
        this.koName = koName;
        this.enName = enName;
        this.introduction = introduction;
        this.techStack = techStack;
    }
}

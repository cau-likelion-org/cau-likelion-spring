package com.example.cau_likelion_spring.blog.domain;

import com.example.cau_likelion_spring.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 외부 블로그 글 큐레이션
 * 업로드 날짜(createdAt)는 BaseTimeEntity에서 상속받음
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Blog extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String thumbnailUrl;

    @Enumerated(EnumType.STRING)
    private BlogCategory category;

    @Lob
    private String summary;

    private String writer;

    /** 원문 블로그 url */
    @Column(nullable = false)
    private String url;

    @Builder
    public Blog(String title, String thumbnailUrl, BlogCategory category, String summary,
                String writer, String url) {
        this.title = title;
        this.thumbnailUrl = thumbnailUrl;
        this.category = category;
        this.summary = summary;
        this.writer = writer;
        this.url = url;
    }
}

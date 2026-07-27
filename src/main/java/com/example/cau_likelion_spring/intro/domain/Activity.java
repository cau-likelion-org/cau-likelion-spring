package com.example.cau_likelion_spring.intro.domain;

import com.example.cau_likelion_spring.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 활동 소개 카드 - 랜딩 페이지 노출용
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Activity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    /** 활동 이미지 url (1개) */
    private String imageUrl;

    /** 한줄 소개 */
    private String introduction;

    private String description;

    private String buttonName;

    /** 버튼 클릭 시 이동할 페이지 (선택 사항이라 null 가능) */
    @Enumerated(EnumType.STRING)
    private PageNavigation pageNavigation;

    @Builder
    public Activity(String name, String imageUrl, String introduction, String description,
                     String buttonName, PageNavigation pageNavigation) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.introduction = introduction;
        this.description = description;
        this.buttonName = buttonName;
        this.pageNavigation = pageNavigation;
    }

    public void update(String name, String imageUrl, String introduction, String description,
                        String buttonName, PageNavigation pageNavigation) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.introduction = introduction;
        this.description = description;
        this.buttonName = buttonName;
        this.pageNavigation = pageNavigation;
    }
}

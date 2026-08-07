package com.example.cau_likelion_spring.member.domain;

import com.example.cau_likelion_spring.global.common.BaseTimeEntity;
import com.example.cau_likelion_spring.organization.domain.Part;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false)
    private String name;

    @Column(length = 100, nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRole role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "part_id")
    private Part part;

    /** 과제 평가 결과 등 푸시 알림을 받을 FCM 토큰. 기기 1개만 지원하며 재등록 시 이전 값을 덮어쓴다. */
    @Column(length = 255)
    private String fcmToken;

    @Builder
    public Member(String name, String email, MemberRole role, Part part) {
        this.name = name;
        this.email = email;
        this.role = role;
        this.part = part;
    }

    public void update(String name, MemberRole role, Part part) {
        this.name = name;
        this.role = role;
        this.part = part;
    }

    public void updateFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }
}

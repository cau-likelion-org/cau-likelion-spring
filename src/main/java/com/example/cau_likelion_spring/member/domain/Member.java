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

    /** 푸시 알림 수신 여부. 기기별 FCM 토큰과 별개로 계정에 귀속되는 설정이라 로그아웃해도 유지된다. */
    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean pushEnabled = true;

    @Builder
    public Member(String name, String email, MemberRole role, Part part) {
        this.name = name;
        this.email = email;
        this.role = role;
        this.part = part;
    }

    public void update(String name, String email, MemberRole role, Part part) {
        this.name = name;
        this.email = email;
        this.role = role;
        this.part = part;
    }

    public void enablePush() {
        this.pushEnabled = true;
    }

    public void disablePush() {
        this.pushEnabled = false;
    }
}

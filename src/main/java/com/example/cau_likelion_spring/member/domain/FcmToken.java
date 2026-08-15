package com.example.cau_likelion_spring.member.domain;

import com.example.cau_likelion_spring.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 구성원의 PWA 푸시 알림 수신용 FCM 토큰. 기기(브라우저)마다 하나씩 발급되므로 구성원 1명이 여러 개를 가질 수 있다.
 * token은 기기 단위로 유일하므로, 같은 토큰으로 다른 구성원이 재등록하면(기기 재사용/계정 전환) 소유자를 그 구성원으로 옮겨 쓴다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FcmToken extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false, unique = true, length = 255)
    private String token;

    @Builder
    public FcmToken(Member member, String token) {
        this.member = member;
        this.token = token;
    }

    public void reassignTo(Member member) {
        this.member = member;
    }
}

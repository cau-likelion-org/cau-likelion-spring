package com.example.cau_likelion_spring.member.domain;

import com.example.cau_likelion_spring.global.common.BaseTimeEntity;
import com.example.cau_likelion_spring.organization.domain.Generation;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회원가입 허용 유저 이메일
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AllowedUserEmail extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generation_id", nullable = false)
    private Generation generation;

    @Column(nullable = false)
    private String allowedEmail;

    /** 해당 이메일이 실제로 가입 완료했는지 */
    @Column(nullable = false)
    private Boolean isJoined;

    @Builder
    public AllowedUserEmail(Generation generation, String allowedEmail, Boolean isJoined) {
        this.generation = generation;
        this.allowedEmail = allowedEmail;
        this.isJoined = isJoined;
    }
}

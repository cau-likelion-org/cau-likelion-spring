package com.example.cau_likelion_spring.recruitment.domain;

import com.example.cau_likelion_spring.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "recruitment_applicant",
        uniqueConstraints = @UniqueConstraint(columnNames = {"round_id", "email"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Applicant extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 가입한 회차. 지원서를 내기 전인 계정도 회차에 묶어야 해서 여기에 둔다.
     * 같은 이메일이라도 회차가 다르면 새로 가입할 수 있다 (round_id + email unique).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "round_id")
    private Round round;

    @Column(length = 10)
    private String name;

    @Column(length = 20)
    private String phone;

    @Column(length = 100)
    private String email;

    @Column(length = 255)
    private String password;

    /**
     * 폼 진입 시각 (지각 판정용).
     * 계정 생성 시 기록하고, 접수 기간 안의 로그인일 때만 갱신한다.
     * 마감 후 로그인(결과 확인 등)으로 덮어쓰면 지각 제출 자격이 사라지므로 갱신하지 않는다.
     */
    private LocalDateTime formEnteredAt;

    @Builder
    public Applicant(Round round, String name, String phone, String email, String password,
                     LocalDateTime formEnteredAt) {
        this.round = round;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.password = password;
        this.formEnteredAt = formEnteredAt;
    }

    public void updateFormEnteredAt(LocalDateTime formEnteredAt) {
        this.formEnteredAt = formEnteredAt;
    }

    /** 암호화된 비밀번호로 바꾼다 */
    public void updatePassword(String password) {
        this.password = password;
    }
}

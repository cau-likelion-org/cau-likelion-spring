package com.example.cau_likelion_spring.attendance.domain;

import com.example.cau_likelion_spring.global.common.BaseTimeEntity;
import com.example.cau_likelion_spring.member.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자별 누적 점수표 (출결 + 과제 감점 종합)
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TotalScore extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private Member member;

    private Integer absent;

    private Integer unauthorizedAbsent;

    private Integer late;

    /** 공결 */
    private Integer excusedCount;

    /** 과제 미제출 */
    private Integer assignmentUnSubmit;

    /** 과제 지각제출 */
    private Integer assignmentLateSubmit;

    private Float total;

    @Builder
    public TotalScore(Member member, Integer absent, Integer unauthorizedAbsent, Integer late,
                       Integer excusedCount, Integer assignmentUnSubmit, Integer assignmentLateSubmit,
                       Float total) {
        this.member = member;
        this.absent = absent;
        this.unauthorizedAbsent = unauthorizedAbsent;
        this.late = late;
        this.excusedCount = excusedCount;
        this.assignmentUnSubmit = assignmentUnSubmit;
        this.assignmentLateSubmit = assignmentLateSubmit;
        this.total = total;
    }
}

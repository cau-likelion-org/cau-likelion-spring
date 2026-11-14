package com.example.cau_likelion_spring.recruitment.domain;

import com.example.cau_likelion_spring.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 지원서.
 * "application"은 의미가 너무 일반적이고(스프링 Application 클래스 등과 헷갈림)
 * 다른 도메인 테이블과 겹칠 수 있어, recruitment_ 접두어를 붙인 테이블명을 명시한다.
 */
@Entity
@Table(name = "recruitment_application")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Application extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id")
    private Applicant applicant;

    @Column(length = 10)
    private String studentNumber;

    @Column(length = 50)
    private String department;

    @Enumerated(EnumType.STRING)
    private Grade grade;

    private Integer semester;

    @Enumerated(EnumType.STRING)
    private EnrollmentStatus enrollment;

    /** 캡처/정보 URL */
    private String capInfo;

    @Enumerated(EnumType.STRING)
    private ApplicationSource source;

    private LocalDateTime submitAt;

    @Builder
    public Application(Applicant applicant, String studentNumber, String department, Grade grade,
                       Integer semester, EnrollmentStatus enrollment, String capInfo,
                       ApplicationSource source, LocalDateTime submitAt) {
        this.applicant = applicant;
        this.studentNumber = studentNumber;
        this.department = department;
        this.grade = grade;
        this.semester = semester;
        this.enrollment = enrollment;
        this.capInfo = capInfo;
        this.source = source;
        this.submitAt = submitAt;
    }
}

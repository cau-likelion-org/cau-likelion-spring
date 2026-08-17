package com.example.cau_likelion_spring.notification.domain;

import com.example.cau_likelion_spring.organization.domain.Part;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 모집 공고 이메일 알림 신청자
 */
@Entity
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecruitmentSubscriber {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String name;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "recruitment_subscriber_interest_part",
            joinColumns = @JoinColumn(name = "subscriber_id"),
            inverseJoinColumns = @JoinColumn(name = "part_id")
    )
    @OnDelete(action = OnDeleteAction.CASCADE) // 구독자 삭제(매년 3월 1일 초기화의 deleteAllInBatch 포함) 시 매핑 row도 같이 삭제되어야 FK 위반이 안 남
    private List<Part> interestParts = new ArrayList<>();

    @CreatedDate
    private LocalDateTime registeredAt;

    @Builder
    public RecruitmentSubscriber(String email, String name, List<Part> interestParts) {
        this.email = email;
        this.name = name;
        this.interestParts = interestParts;
    }
}

package com.example.cau_likelion_spring.gallery.history.domain;

import com.example.cau_likelion_spring.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HistoryImage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "history_id", nullable = false)
    private History history;

    private String imageUrl;

    @Builder
    public HistoryImage(History history, String imageUrl) {
        this.history = history;
        this.imageUrl = imageUrl;
    }
}

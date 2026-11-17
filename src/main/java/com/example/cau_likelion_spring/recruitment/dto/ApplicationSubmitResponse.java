package com.example.cau_likelion_spring.recruitment.dto;

import com.example.cau_likelion_spring.recruitment.domain.Application;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 지원서 제출 응답 (7.4.3 제출 완료 화면).
 */
@Getter
@Builder
public class ApplicationSubmitResponse {

    private LocalDateTime submitAt;
    private String part;

    public static ApplicationSubmitResponse from(Application application) {
        return ApplicationSubmitResponse.builder()
                .submitAt(application.getSubmitAt())
                .part(application.getRoundPart().getPart())
                .build();
    }
}

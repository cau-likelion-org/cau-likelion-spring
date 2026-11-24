package com.example.cau_likelion_spring.recruitment.dto;

import com.example.cau_likelion_spring.recruitment.domain.Application;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 지원자 본인의 제출 내역 (7.3.3).
 * 제출 여부와 제출 일시·지원 파트만 알려주고, 지원서 내용은 반환하지 않는다.
 */
@Getter
@Builder
public class MyApplicationResponse {

    /** 이번 회차에 제출한 지원서가 있으면 true */
    private boolean submitted;

    /** 제출 일시 (미제출이면 null) */
    private LocalDateTime submitAt;

    /** 지원 파트 (미제출이면 null) */
    private String part;

    /** 지금이 접수 기간(시작~마감) 안이면 true. 미제출자에게 [지원서 작성하기]를 보여줄지 판단하는 데 쓴다 */
    private boolean applyOpen;

    public static MyApplicationResponse submitted(Application application, boolean applyOpen) {
        return MyApplicationResponse.builder()
                .submitted(true)
                .submitAt(application.getSubmitAt())
                .part(application.getRoundPart().getPart())
                .applyOpen(applyOpen)
                .build();
    }

    public static MyApplicationResponse notSubmitted(boolean applyOpen) {
        return MyApplicationResponse.builder()
                .submitted(false)
                .applyOpen(applyOpen)
                .build();
    }
}

package com.example.cau_likelion_spring.recruitment.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 학번 중복 조회 응답.
 * 지원 여부만 알려주고, 이름·제출 일시 등 지원서 정보는 노출하지 않는다.
 */
@Getter
@Builder
public class StudentNumberCheckResponse {

    /** 이번 회차에 같은 학번으로 제출된 지원서가 있으면 true */
    private boolean exists;

    public static StudentNumberCheckResponse of(boolean exists) {
        return StudentNumberCheckResponse.builder()
                .exists(exists)
                .build();
    }
}

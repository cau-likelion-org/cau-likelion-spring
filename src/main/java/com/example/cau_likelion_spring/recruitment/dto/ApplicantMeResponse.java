package com.example.cau_likelion_spring.recruitment.dto;

import com.example.cau_likelion_spring.recruitment.domain.Applicant;
import lombok.Builder;
import lombok.Getter;

/** 지원자 본인 기본 정보 (지원서 1단계에서 수정 불가로 표시) */
@Getter
@Builder
public class ApplicantMeResponse {

    private String name;
    private String phone;
    private String email;

    public static ApplicantMeResponse from(Applicant applicant) {
        return ApplicantMeResponse.builder()
                .name(applicant.getName())
                .phone(applicant.getPhone())
                .email(applicant.getEmail())
                .build();
    }
}

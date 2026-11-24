package com.example.cau_likelion_spring.recruitment.dto;

import com.example.cau_likelion_spring.recruitment.domain.ApplicationSource;
import com.example.cau_likelion_spring.recruitment.domain.EnrollmentStatus;
import com.example.cau_likelion_spring.recruitment.domain.Grade;
import lombok.Builder;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * 지원서 1단계 선택지 조회 응답 (학년, 학적, 지원 경로, 지원 파트 드롭다운).
 * 화면에는 label을 보여주고, 제출할 때는 value(파트는 id)를 보낸다.
 */
@Getter
@Builder
public class FormOptionsResponse {

    private List<OptionItem> grades;
    private List<OptionItem> enrollments;
    private List<OptionItem> sources;
    private List<RoundPartResponse> parts;

    public static FormOptionsResponse of(List<RoundPartResponse> parts) {
        return FormOptionsResponse.builder()
                .grades(Arrays.stream(Grade.values())
                        .map(grade -> OptionItem.of(grade.name(), grade.getLabel()))
                        .toList())
                .enrollments(Arrays.stream(EnrollmentStatus.values())
                        .map(enrollment -> OptionItem.of(enrollment.name(), enrollment.getLabel()))
                        .toList())
                .sources(Arrays.stream(ApplicationSource.values())
                        .map(source -> OptionItem.of(source.name(), source.getLabel()))
                        .toList())
                .parts(parts)
                .build();
    }

    @Getter
    @Builder
    public static class OptionItem {

        private String value;
        private String label;

        public static OptionItem of(String value, String label) {
            return OptionItem.builder()
                    .value(value)
                    .label(label)
                    .build();
        }
    }
}

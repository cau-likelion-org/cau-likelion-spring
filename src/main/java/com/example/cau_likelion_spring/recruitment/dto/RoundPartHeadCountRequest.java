package com.example.cau_likelion_spring.recruitment.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 모집 기수의 파트별 정원 등록(POST)·수정(PATCH) 요청.
 * 요청에 포함된 파트만 처리한다.
 */
@Getter
@NoArgsConstructor
public class RoundPartHeadCountRequest {

    @NotEmpty
    private List<@Valid @NotNull PartHeadCountItem> parts;

    @Getter
    @NoArgsConstructor
    public static class PartHeadCountItem {

        /** 기수 파트 id (GET 응답의 parts.partId) */
        @NotNull
        private Long partId;

        /** 모집 정원 */
        @NotNull
        @Positive
        private Integer headCount;
    }
}

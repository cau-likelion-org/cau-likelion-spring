package com.example.cau_likelion_spring.intro.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "랜딩페이지에 노출할 프로젝트 지정 요청 - 여기 담긴 id의 프로젝트만 노출(true)되고 나머지는 전부 비노출(false) 처리됩니다.")
public record ExposedProjectsRequest(

        @Schema(description = "랜딩페이지에 노출할 프로젝트 ID 목록 (빈 배열이면 전체 비노출 처리)", example = "[1, 3, 5]")
        @NotNull
        List<Long> exposedProjectIds
) {
}

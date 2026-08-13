package com.example.cau_likelion_spring.notification.dto;

import com.example.cau_likelion_spring.organization.domain.Part;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "모집 알림 신청 시 선택 가능한 관심 파트")
public record AvailablePartResponse(

        @Schema(description = "파트 id", example = "6")
        Long id,

        @Schema(description = "파트 이름", example = "Backend")
        String name
) {

    public static AvailablePartResponse of(Part part) {
        return new AvailablePartResponse(part.getId(), part.getName());
    }
}

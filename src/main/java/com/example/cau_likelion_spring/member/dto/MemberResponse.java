package com.example.cau_likelion_spring.member.dto;

import com.example.cau_likelion_spring.member.domain.Member;
import com.example.cau_likelion_spring.member.domain.MemberRole;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "구성원 응답")
public record MemberResponse(

        @Schema(description = "구성원 ID", example = "1")
        Long id,

        @Schema(description = "이름", example = "홍길동")
        String name,

        @Schema(description = "이메일", example = "hong@example.com")
        String email,

        @Schema(description = "역할")
        MemberRole role,

        @Schema(description = "파트 ID", example = "1")
        Long partId,

        @Schema(description = "파트 이름", example = "Backend")
        String partName,

        @Schema(description = "기수 (소속 파트가 없으면 null)", example = "13")
        Integer generationNumber
) {

    public static MemberResponse from(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getName(),
                member.getEmail(),
                member.getRole(),
                member.getPart() != null ? member.getPart().getId() : null,
                member.getPart() != null ? member.getPart().getName() : null,
                member.getPart() != null ? member.getPart().getGeneration().getNumber() : null
        );
    }
}

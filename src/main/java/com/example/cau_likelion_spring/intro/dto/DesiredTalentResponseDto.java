package com.example.cau_likelion_spring.intro.dto;

import com.example.cau_likelion_spring.intro.domain.DesiredTalent;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DesiredTalentResponseDto {

    private Long id;
    private String partName;
    private String content;

    public static DesiredTalentResponseDto from(DesiredTalent desiredTalent) {
        return DesiredTalentResponseDto.builder()
                .id(desiredTalent.getId())
                .partName(desiredTalent.getPartName())
                .content(desiredTalent.getContent())
                .build();
    }
}

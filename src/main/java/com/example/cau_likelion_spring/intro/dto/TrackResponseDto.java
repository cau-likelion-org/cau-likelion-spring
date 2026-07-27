package com.example.cau_likelion_spring.intro.dto;

import com.example.cau_likelion_spring.intro.domain.Track;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class TrackResponseDto {

    private Long id;
    private String koName;
    private String enName;
    private String introduction;
    private List<String> techStack;

    public static TrackResponseDto from(Track track) {
        return TrackResponseDto.builder()
                .id(track.getId())
                .koName(track.getKoName())
                .enName(track.getEnName())
                .introduction(track.getIntroduction())
                .techStack(track.getTechStack())
                .build();
    }
}

package com.example.cau_likelion_spring.intro.dto;

import com.example.cau_likelion_spring.intro.domain.Activity;
import com.example.cau_likelion_spring.intro.domain.PageNavigation;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ActivityResponseDto {

    private Long id;
    private String name;
    private String imageUrl;
    private String introduction;
    private String description;
    private String buttonName;
    private PageNavigation pageNavigation;

    public static ActivityResponseDto from(Activity activity) {
        return ActivityResponseDto.builder()
                .id(activity.getId())
                .name(activity.getName())
                .imageUrl(activity.getImageUrl())
                .introduction(activity.getIntroduction())
                .description(activity.getDescription())
                .buttonName(activity.getButtonName())
                .pageNavigation(activity.getPageNavigation())
                .build();
    }
}

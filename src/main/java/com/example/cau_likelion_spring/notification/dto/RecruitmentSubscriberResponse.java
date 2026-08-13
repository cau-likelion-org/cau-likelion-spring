package com.example.cau_likelion_spring.notification.dto;

import com.example.cau_likelion_spring.notification.domain.RecruitmentSubscriber;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "모집 알림 구독자 응답")
public record RecruitmentSubscriberResponse(

        @Schema(description = "구독자 ID", example = "1")
        Long id,

        @Schema(description = "구독 이메일")
        String email,

        @Schema(description = "신청자 이름")
        String name,

        @Schema(description = "관심 파트 목록")
        List<AvailablePartResponse> interestParts,

        @Schema(description = "신청 일시")
        LocalDateTime registeredAt
) {

    public static RecruitmentSubscriberResponse of(RecruitmentSubscriber subscriber) {
        return new RecruitmentSubscriberResponse(
                subscriber.getId(),
                subscriber.getEmail(),
                subscriber.getName(),
                subscriber.getInterestParts().stream().map(AvailablePartResponse::of).toList(),
                subscriber.getRegisteredAt()
        );
    }
}

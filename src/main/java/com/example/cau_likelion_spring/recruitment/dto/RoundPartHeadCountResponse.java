package com.example.cau_likelion_spring.recruitment.dto;

import com.example.cau_likelion_spring.organization.domain.Part;
import com.example.cau_likelion_spring.recruitment.domain.RoundPart;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 모집 기수의 파트 목록 + 파트별 정원 응답.
 * 아직 정원을 등록하지 않은 파트는 roundPartId·headCount가 null이다.
 */
@Getter
@Builder
public class RoundPartHeadCountResponse {

    /** 모집 기수 번호 (현재 회차에 연결된 기수) */
    private Integer generation;

    /** 정원이 등록되는 현재 회차 id */
    private Long roundId;

    private List<PartHeadCountDetail> parts;

    @Getter
    @Builder
    public static class PartHeadCountDetail {

        private Long partId;
        private String partName;
        private Long roundPartId;
        private Integer headCount;

        public static PartHeadCountDetail of(Part part, RoundPart roundPart) {
            return PartHeadCountDetail.builder()
                    .partId(part.getId())
                    .partName(part.getName())
                    .roundPartId(roundPart == null ? null : roundPart.getId())
                    .headCount(roundPart == null ? null : roundPart.getHeadCount())
                    .build();
        }
    }
}

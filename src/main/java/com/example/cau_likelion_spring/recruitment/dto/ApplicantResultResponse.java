package com.example.cau_likelion_spring.recruitment.dto;

import com.example.cau_likelion_spring.recruitment.domain.Round;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 지원자 결과 확인 응답 (7.5.1).
 * PASS·FAIL은 회장단이 작성한 결과 안내 문구(템플릿)를 지원자 값으로 치환한 것이고,
 * 나머지 상태는 명세에 고정된 안내 문구를 그대로 담는다 (DB에 저장하지 않음).
 * message는 줄바꿈(\n)을 포함한 평문이다.
 */
@Getter
@Builder
@Schema(description = "지원자 결과 확인")
public class ApplicantResultResponse {

    @Schema(description = "어느 전형의 결과인지. 서류 불합격자는 최종 결과를 조회해도 DOCUMENT로 내려간다", example = "DOCUMENT")
    private Stage stage;

    @Schema(description = "회차명", example = "14기")
    private String roundName;

    @Schema(description = "결과 상태", example = "PASS")
    private Status status;

    @Schema(description = "화면에 보여줄 안내 문구 (줄바꿈 \\n 포함 평문)")
    private String message;

    /** 결과 안내 문구(템플릿)를 치환한 합격·불합격 응답 */
    public static ApplicantResultResponse of(Stage stage, Round round, Status status, String message) {
        return ApplicantResultResponse.builder()
                .stage(stage)
                .roundName(round.getName())
                .status(status)
                .message(message)
                .build();
    }

    /** 명세에 고정된 안내 문구를 쓰는 응답 (평가 중, 제출 내역 없음, 확인 기간 종료) */
    public static ApplicantResultResponse fixed(Stage stage, Round round, Status status) {
        return of(stage, round, status, status.fixedMessageOf(stage));
    }

    @Getter
    @RequiredArgsConstructor
    public enum Stage {
        DOCUMENT("서류"),
        FINAL("면접");

        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    public enum Status {
        NOT_SUBMITTED("제출 내역이 없습니다."),
        EVALUATING("%s전형 평가 중입니다."),
        CHECK_PERIOD_ENDED("결과 확인 기간이 종료되었습니다."),
        PASS(null),   // 결과 안내 문구(템플릿) 사용
        FAIL(null);   // 결과 안내 문구(템플릿) 사용

        /** 명세에 고정된 안내 문구 (PASS·FAIL은 템플릿을 쓰므로 null) */
        private final String fixedMessage;

        public String fixedMessageOf(Stage stage) {
            return this == EVALUATING ? String.format(fixedMessage, stage.getLabel()) : fixedMessage;
        }
    }
}

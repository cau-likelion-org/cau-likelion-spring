package com.example.cau_likelion_spring.recruitment.dto;

import com.example.cau_likelion_spring.recruitment.domain.ResultTemplateType;
import com.example.cau_likelion_spring.recruitment.domain.ResultTemplateVariable;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Arrays;
import java.util.List;

/** 백오피스 결과 안내 문구 템플릿 (종류 하나) */
@Schema(description = "결과 안내 문구 템플릿")
public record ResultTemplateResponse(

        @Schema(description = "템플릿 종류 (DOCUMENT_PASS: 서류 합격, DOCUMENT_FAIL: 서류 불합격, "
                + "FINAL_PASS: 최종 합격, FINAL_FAIL: 최종 불합격)", example = "DOCUMENT_PASS")
        ResultTemplateType type,

        @Schema(description = "템플릿 이름 (화면 표시용)", example = "서류 합격")
        String label,

        @Schema(description = "문구. 변수는 {이름}처럼 토큰 그대로 들어 있습니다.",
                example = "{이름}님, 서류 전형에 합격하셨습니다.\n면접 20분 전까지 대기 장소에 도착해주세요.")
        String text,

        @Schema(description = "기본 문구 여부 (아직 저장한 적이 없으면 true)", example = "false")
        boolean usingDefault,

        @Schema(description = "변수 삽입 버튼 목록 (enabled=false면 버튼 비활성)")
        List<VariableButton> variables
) {
    public static ResultTemplateResponse of(ResultTemplateType type, String text, boolean usingDefault) {
        List<VariableButton> variables = Arrays.stream(ResultTemplateVariable.values())
                .map(variable -> new VariableButton(variable, variable.getToken(), type.allows(variable)))
                .toList();
        return new ResultTemplateResponse(type, type.getLabel(), text, usingDefault, variables);
    }

    @Schema(description = "변수 삽입 버튼")
    public record VariableButton(

            @Schema(description = "변수 (NAME: 이름, INTERVIEW_DATE: 면접 일시, INTERVIEW_SPOT: 면접 대기 장소)",
                    example = "INTERVIEW_DATE")
            ResultTemplateVariable variable,

            @Schema(description = "본문에 삽입할 토큰 (버튼 문구로도 사용)", example = "{면접 일시}")
            String token,

            @Schema(description = "이 템플릿에서 사용 가능 여부. 면접 변수는 서류 합격 템플릿에서만 true", example = "true")
            boolean enabled
    ) {
    }
}

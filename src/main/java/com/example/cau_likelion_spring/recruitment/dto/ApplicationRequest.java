package com.example.cau_likelion_spring.recruitment.dto;

import com.example.cau_likelion_spring.recruitment.domain.ApplicationSource;
import com.example.cau_likelion_spring.recruitment.domain.EnrollmentStatus;
import com.example.cau_likelion_spring.recruitment.domain.Grade;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 지원서 제출 요청 (1단계 정보 + 면접 가능 시간 + 2단계 답변을 한 번에).
 * 서버 임시저장이 없으므로(7.4.4) 제출은 한 번에 처리하고, 1단계 항목도 여기서 다시 전부 검증한다.
 */
@Getter
@NoArgsConstructor
public class ApplicationRequest {

    // TODO: 지원자 로그인(7.3) 완료 후 제거하고 지원자 토큰으로 식별 - 지금은 남의 id로도 제출 가능
    @NotNull
    private Long applicantId;

    @NotNull
    private Long roundPartId;

    @NotBlank
    @Pattern(regexp = "^\\d{8}$", message = "학번은 8자리 숫자여야 합니다.")
    private String studentNumber;

    @NotBlank
    @Size(max = 50)
    private String department;

    @NotNull
    private Grade grade;

    @NotNull
    @Min(0)
    private Integer semester;

    @NotNull
    private EnrollmentStatus enrollment;

    /** 포탈 내정보수정 캡처 URL (업로드 API가 돌려준 URL) */
    @NotBlank
    @Size(max = 255)
    private String capInfo;

    @NotNull
    private ApplicationSource source;

    @NotEmpty
    private List<@NotNull Long> interviewTimeIds;

    @Valid
    private List<AnswerRequest> answers;
}

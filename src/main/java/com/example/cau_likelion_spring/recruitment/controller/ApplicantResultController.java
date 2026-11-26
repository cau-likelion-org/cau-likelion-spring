package com.example.cau_likelion_spring.recruitment.controller;

import com.example.cau_likelion_spring.recruitment.dto.ApplicantResultResponse;
import com.example.cau_likelion_spring.recruitment.service.ApplicantResultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Recruitment Applicant Result", description = "리크루팅 지원자 결과 확인 API")
@RestController
@RequestMapping("/api/recruitment/results")
@RequiredArgsConstructor
public class ApplicantResultController {

    private final ApplicantResultService applicantResultService;

    private static final String STATUS_RULE = "status는 NOT_SUBMITTED(제출 내역 없음), EVALUATING(평가 중: 공개 전·보류·미입력), "
            + "CHECK_PERIOD_ENDED(결과 확인 기간 종료), PASS, FAIL 중 하나이며, message는 화면에 그대로 보여줄 문구입니다 "
            + "(줄바꿈 \\n 포함 평문). PASS·FAIL은 백오피스에서 작성한 결과 안내 문구를 지원자 값으로 치환한 것입니다. "
            + "결과는 조회 시점 기준이라 공개 후 수정되면 바로 반영됩니다. 지원자 토큰이 필요합니다.";

    @Operation(summary = "서류 결과 조회", description = "로그인한 지원자의 이번 회차 서류 결과를 반환합니다. " + STATUS_RULE)
    @GetMapping("/document")
    public ResponseEntity<ApplicantResultResponse> getDocumentResult(@AuthenticationPrincipal Long applicantId) {
        return ResponseEntity.ok(applicantResultService.getDocumentResult(applicantId));
    }

    @Operation(summary = "최종 결과 조회",
            description = "로그인한 지원자의 이번 회차 최종 결과를 반환합니다. "
                    + "서류 불합격자는 서류 불합격 결과를 그대로 반환하며(stage=DOCUMENT), "
                    + "서류 결과가 아직 공개 전이거나 보류면 서류전형 평가 중으로 반환합니다. " + STATUS_RULE)
    @GetMapping("/final")
    public ResponseEntity<ApplicantResultResponse> getFinalResult(@AuthenticationPrincipal Long applicantId) {
        return ResponseEntity.ok(applicantResultService.getFinalResult(applicantId));
    }
}

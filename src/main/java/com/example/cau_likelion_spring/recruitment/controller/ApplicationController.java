package com.example.cau_likelion_spring.recruitment.controller;

import com.example.cau_likelion_spring.recruitment.dto.ApplicationRequest;
import com.example.cau_likelion_spring.recruitment.dto.ApplicationSubmitResponse;
import com.example.cau_likelion_spring.recruitment.dto.FormOptionsResponse;
import com.example.cau_likelion_spring.recruitment.dto.MyApplicationResponse;
import com.example.cau_likelion_spring.recruitment.dto.StudentNumberCheckRequest;
import com.example.cau_likelion_spring.recruitment.dto.StudentNumberCheckResponse;
import com.example.cau_likelion_spring.recruitment.service.ApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Recruitment Application", description = "리크루팅 지원서 작성·제출 API")
@RestController
@RequestMapping("/api/recruitment/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @Operation(summary = "지원서 1단계 선택지 조회",
            description = "학년, 학적, 지원 경로, 현재 회차의 지원 파트 선택지를 반환합니다. "
                    + "화면에는 label을 표시하고, 제출 시에는 value(파트는 id)를 보냅니다.")
    @GetMapping("/form-options")
    public ResponseEntity<FormOptionsResponse> getFormOptions() {
        return ResponseEntity.ok(applicationService.getFormOptions());
    }

    @Operation(summary = "지원서 제출",
            description = "지원자 토큰이 필요하며, 토큰의 지원자로 제출됩니다. "
                    + "1단계 정보, 면접 가능 시간, 2단계 답변을 한 번에 제출합니다. "
                    + "장문·링크·파일 문항은 answer(파일은 업로드 API가 돌려준 URL), 객관식은 optionIds로 보냅니다. "
                    + "마감 후라도 폼 진입 시각이 마감 전이면 지각 제출로 접수됩니다.")
    @PostMapping
    public ResponseEntity<ApplicationSubmitResponse> submit(@AuthenticationPrincipal Long applicantId,
                                                            @Valid @RequestBody ApplicationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(applicationService.submit(applicantId, request));
    }

    @Operation(summary = "학번 중복 조회",
            description = "지원자 토큰이 필요합니다. "
                    + "이번 회차에 같은 학번으로 제출된 지원서가 있는지 확인합니다. 학번은 8자리 숫자입니다.")
    @PostMapping("/student-number/check")
    public ResponseEntity<StudentNumberCheckResponse> checkStudentNumber(
            @Valid @RequestBody StudentNumberCheckRequest request) {
        return ResponseEntity.ok(applicationService.checkStudentNumber(request));
    }

    @Operation(summary = "지원자 본인 제출 내역 조회",
            description = "지원자 토큰이 필요합니다. 이번 회차에 제출한 지원서가 있으면 제출 일시와 지원 파트를, "
                    + "없으면 submitted=false를 반환합니다. applyOpen은 지금이 접수 기간인지 여부입니다.")
    @GetMapping("/me")
    public ResponseEntity<MyApplicationResponse> getMyApplication(@AuthenticationPrincipal Long applicantId) {
        return ResponseEntity.ok(applicationService.getMyApplication(applicantId));
    }
}

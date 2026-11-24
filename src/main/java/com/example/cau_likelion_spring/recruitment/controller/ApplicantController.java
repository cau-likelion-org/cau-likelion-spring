package com.example.cau_likelion_spring.recruitment.controller;

import com.example.cau_likelion_spring.recruitment.dto.ApplicantLoginRequest;
import com.example.cau_likelion_spring.recruitment.dto.ApplicantLoginResponse;
import com.example.cau_likelion_spring.recruitment.dto.ApplicantMeResponse;
import com.example.cau_likelion_spring.recruitment.dto.ApplicantSignupRequest;
import com.example.cau_likelion_spring.recruitment.dto.ApplicantSignupResponse;
import com.example.cau_likelion_spring.recruitment.service.ApplicantService;
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

@Tag(name = "Recruitment Applicant", description = "리크루팅 지원자 계정 API")
@RestController
@RequestMapping("/api/recruitment/applicants")
@RequiredArgsConstructor
public class ApplicantController {

    private final ApplicantService applicantService;

    @Operation(summary = "지원자 계정 생성 (0단계)",
            description = "이메일 인증을 마친 뒤 verificationToken(용도 SIGNUP)과 함께 요청합니다. "
                    + "접수 기간에만 가능하며, 성공하면 지원자 토큰(accessToken)을 반환합니다. "
                    + "이번 회차에 같은 이메일의 계정이 이미 있으면 409입니다.")
    @PostMapping
    public ResponseEntity<ApplicantSignupResponse> signup(@Valid @RequestBody ApplicantSignupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(applicantService.signup(request));
    }

    @Operation(summary = "지원자 로그인",
            description = "이번 회차 계정의 이메일·비밀번호로 로그인하고 지원자 토큰(accessToken)을 반환합니다. "
                    + "이메일이 없거나 비밀번호가 틀리면 구분 없이 401입니다.")
    @PostMapping("/login")
    public ResponseEntity<ApplicantLoginResponse> login(@Valid @RequestBody ApplicantLoginRequest request) {
        return ResponseEntity.ok(applicantService.login(request));
    }

    @Operation(summary = "지원자 본인 정보 조회",
            description = "지원자 토큰으로 본인의 이름, 전화번호, 이메일을 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<ApplicantMeResponse> getMe(@AuthenticationPrincipal Long applicantId) {
        return ResponseEntity.ok(applicantService.getMe(applicantId));
    }
}

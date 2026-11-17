package com.example.cau_likelion_spring.recruitment.controller;

import com.example.cau_likelion_spring.recruitment.dto.FormOptionsResponse;
import com.example.cau_likelion_spring.recruitment.dto.StudentNumberCheckRequest;
import com.example.cau_likelion_spring.recruitment.dto.StudentNumberCheckResponse;
import com.example.cau_likelion_spring.recruitment.service.ApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

    // TODO: 지원자 로그인(7.3) 완료 후 지원자 토큰 인증 필요로 변경 (SecurityConfig permitAll 제거)
    //       현재는 누구나 학번으로 지원 여부를 조회할 수 있어 배포 전 반드시 막아야 함
    @Operation(summary = "학번 중복 조회",
            description = "이번 회차에 같은 학번으로 제출된 지원서가 있는지 확인합니다. 학번은 8자리 숫자입니다.")
    @PostMapping("/student-number/check")
    public ResponseEntity<StudentNumberCheckResponse> checkStudentNumber(
            @Valid @RequestBody StudentNumberCheckRequest request) {
        return ResponseEntity.ok(applicationService.checkStudentNumber(request));
    }
}

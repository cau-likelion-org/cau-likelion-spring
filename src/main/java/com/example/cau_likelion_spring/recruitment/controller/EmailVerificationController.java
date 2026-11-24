package com.example.cau_likelion_spring.recruitment.controller;

import com.example.cau_likelion_spring.recruitment.dto.EmailVerificationConfirmRequest;
import com.example.cau_likelion_spring.recruitment.dto.EmailVerificationConfirmResponse;
import com.example.cau_likelion_spring.recruitment.dto.EmailVerificationSendRequest;
import com.example.cau_likelion_spring.recruitment.dto.EmailVerificationSendResponse;
import com.example.cau_likelion_spring.recruitment.service.EmailVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Recruitment Email Verification", description = "리크루팅 지원자 이메일 인증 API")
@RestController
@RequestMapping("/api/recruitment/email-verifications")
@RequiredArgsConstructor
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

    @Operation(summary = "인증번호 발송",
            description = "6자리 인증번호를 메일로 보냅니다. 유효시간은 10분이며 expiresAt으로 만료 시각을 알려줍니다. "
                    + "같은 이메일·용도로는 60초가 지나야 다시 보낼 수 있고(429), 다시 보내면 이전 인증번호는 무효가 됩니다.")
    @PostMapping
    public ResponseEntity<EmailVerificationSendResponse> send(
            @Valid @RequestBody EmailVerificationSendRequest request) {
        return ResponseEntity.ok(emailVerificationService.send(request));
    }

    @Operation(summary = "인증번호 확인",
            description = "인증번호가 맞으면 인증 완료 토큰(verificationToken, 30분 유효)을 반환합니다. "
                    + "5번 틀리면 해당 인증번호는 무효가 되어 다시 발송받아야 합니다.")
    @PostMapping("/confirm")
    public ResponseEntity<EmailVerificationConfirmResponse> confirm(
            @Valid @RequestBody EmailVerificationConfirmRequest request) {
        return ResponseEntity.ok(emailVerificationService.confirm(request));
    }
}

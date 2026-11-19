package com.example.cau_likelion_spring.recruitment.controller;

import com.example.cau_likelion_spring.global.config.SecurityConfig;
import com.example.cau_likelion_spring.global.jwt.JwtAuthenticationFilter;
import com.example.cau_likelion_spring.global.jwt.JwtTokenProvider;
import com.example.cau_likelion_spring.recruitment.dto.EmailVerificationConfirmResponse;
import com.example.cau_likelion_spring.recruitment.dto.EmailVerificationSendResponse;
import com.example.cau_likelion_spring.recruitment.service.EmailVerificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 이메일 인증 API는 로그인 전에 쓰므로 토큰 없이 호출할 수 있어야 한다 */
@WebMvcTest(controllers = EmailVerificationController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtTokenProvider.class})
@TestPropertySource(properties = {
        "jwt.secret=test-secret-key-for-jwt-token-provider-test-0123456789",
        "jwt.access-token-expiration=1h",
        "jwt.refresh-token-expiration=14d",
        "jwt.signup-token-expiration=10m",
        "jwt.applicant-token-expiration=6h",
        "jwt.email-verification-token-expiration=30m",
        "app.frontend-base-url=http://localhost:3000"
})
class EmailVerificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmailVerificationService emailVerificationService;

    @Test
    void 토큰_없이_인증번호를_발송할_수_있다() throws Exception {
        given(emailVerificationService.send(any()))
                .willReturn(EmailVerificationSendResponse.of(LocalDateTime.of(2026, 11, 19, 14, 10)));

        mockMvc.perform(post("/api/recruitment/email-verifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"applicant@cau.ac.kr\",\"purpose\":\"SIGNUP\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.expiresAt").value("2026-11-19T14:10:00"));
    }

    @Test
    void 토큰_없이_인증번호를_확인할_수_있다() throws Exception {
        given(emailVerificationService.confirm(any()))
                .willReturn(EmailVerificationConfirmResponse.of("verification-token"));

        mockMvc.perform(post("/api/recruitment/email-verifications/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"applicant@cau.ac.kr\",\"purpose\":\"SIGNUP\",\"code\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificationToken").value("verification-token"));
    }

    @Test
    void 이메일_형식이_아니거나_인증번호가_6자리_숫자가_아니면_400() throws Exception {
        mockMvc.perform(post("/api/recruitment/email-verifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"not-an-email\",\"purpose\":\"SIGNUP\"}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/recruitment/email-verifications/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"applicant@cau.ac.kr\",\"purpose\":\"SIGNUP\",\"code\":\"12345\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(emailVerificationService);
    }
}

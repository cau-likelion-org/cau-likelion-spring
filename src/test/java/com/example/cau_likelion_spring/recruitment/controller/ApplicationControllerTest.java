package com.example.cau_likelion_spring.recruitment.controller;

import com.example.cau_likelion_spring.global.config.SecurityConfig;
import com.example.cau_likelion_spring.global.jwt.JwtAuthenticationFilter;
import com.example.cau_likelion_spring.global.jwt.JwtTokenProvider;
import com.example.cau_likelion_spring.member.domain.MemberRole;
import com.example.cau_likelion_spring.recruitment.dto.ApplicationSubmitResponse;
import com.example.cau_likelion_spring.recruitment.dto.StudentNumberCheckResponse;
import com.example.cau_likelion_spring.recruitment.service.ApplicationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 지원서 제출·학번 중복 조회는 지원자 토큰으로만 호출할 수 있고, 제출은 토큰의 지원자로 처리되는지 확인 */
@WebMvcTest(controllers = ApplicationController.class)
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
class ApplicationControllerTest {

    private static final String SUBMIT_URL = "/api/recruitment/applications";
    private static final String CHECK_URL = "/api/recruitment/applications/student-number/check";

    private static final String SUBMIT_BODY = """
            {"roundPartId":1,"studentNumber":"20261234","department":"소프트웨어학부","grade":"FIRST",
             "semester":1,"enrollment":"ENROLLED","capInfo":"https://bucket/cap.png","source":"HOMEPAGE",
             "interviewTimeIds":[1]}""";
    private static final String CHECK_BODY = "{\"studentNumber\":\"20261234\"}";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private ApplicationService applicationService;

    /** SecurityConfigApplicantTokenTest와 같은 이유로 Security 체인 밖 필터 등록을 끈다 */
    @TestConfiguration
    static class DisableServletFilterRegistration {
        @Bean
        FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration(JwtAuthenticationFilter filter) {
            FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
            registration.setEnabled(false);
            return registration;
        }
    }

    @Test
    void 지원자_토큰으로_제출하면_토큰의_지원자로_처리한다() throws Exception {
        given(applicationService.submit(eq(7L), any()))
                .willReturn(ApplicationSubmitResponse.builder().part("백엔드").build());

        mockMvc.perform(post(SUBMIT_URL)
                        .header("Authorization", "Bearer " + jwtTokenProvider.createApplicantToken(7L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(SUBMIT_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.part").value("백엔드"));

        verify(applicationService).submit(eq(7L), any());
    }

    @Test
    void 요청_본문에_applicantId를_넣어도_토큰의_지원자로_처리한다() throws Exception {
        given(applicationService.submit(eq(7L), any()))
                .willReturn(ApplicationSubmitResponse.builder().part("백엔드").build());

        mockMvc.perform(post(SUBMIT_URL)
                        .header("Authorization", "Bearer " + jwtTokenProvider.createApplicantToken(7L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(SUBMIT_BODY.replaceFirst("\\{", "{\"applicantId\":99,")))
                .andExpect(status().isCreated());

        verify(applicationService).submit(eq(7L), any());
    }

    @Test
    void 지원자_토큰으로_학번_중복을_조회한다() throws Exception {
        given(applicationService.checkStudentNumber(any())).willReturn(StudentNumberCheckResponse.of(true));

        mockMvc.perform(post(CHECK_URL)
                        .header("Authorization", "Bearer " + jwtTokenProvider.createApplicantToken(7L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CHECK_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exists").value(true));
    }

    @ParameterizedTest
    @ValueSource(strings = {SUBMIT_URL, CHECK_URL})
    void 회원_토큰이나_토큰_없이_호출하면_403(String url) throws Exception {
        String body = url.equals(SUBMIT_URL) ? SUBMIT_BODY : CHECK_BODY;
        String memberToken = jwtTokenProvider.createAccessToken(7L, MemberRole.BABY_LION);

        mockMvc.perform(post(url).header("Authorization", "Bearer " + memberToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
        mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());

        verifyNoInteractions(applicationService);
    }
}

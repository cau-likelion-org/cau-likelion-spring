package com.example.cau_likelion_spring.recruitment.controller;

import com.example.cau_likelion_spring.global.config.SecurityConfig;
import com.example.cau_likelion_spring.global.jwt.JwtAuthenticationFilter;
import com.example.cau_likelion_spring.global.jwt.JwtTokenProvider;
import com.example.cau_likelion_spring.member.domain.MemberRole;
import com.example.cau_likelion_spring.recruitment.dto.ApplicantLoginResponse;
import com.example.cau_likelion_spring.recruitment.dto.ApplicantMeResponse;
import com.example.cau_likelion_spring.recruitment.dto.ApplicantSignupResponse;
import com.example.cau_likelion_spring.recruitment.service.ApplicantService;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 계정 생성·로그인은 토큰 없이, 본인 정보 조회는 지원자 토큰으로만 호출할 수 있는지 확인 */
@WebMvcTest(controllers = ApplicantController.class)
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
class ApplicantControllerTest {

    private static final String VALID_BODY = """
            {"name":"김멋사","phone":"01012345678","email":"applicant@cau.ac.kr",
             "password":"password1!","privacyAgreed":true,"verificationToken":"token"}""";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private ApplicantService applicantService;

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
    void 토큰_없이_계정을_생성할_수_있다() throws Exception {
        given(applicantService.signup(any())).willReturn(ApplicantSignupResponse.of("applicant-token"));

        mockMvc.perform(post("/api/recruitment/applicants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("applicant-token"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "\"phone\":\"010-1234-5678\"",
            "\"password\":\"short1!\"",
            "\"password\":\"한글비밀번호12345\"",
            "\"privacyAgreed\":false",
            "\"name\":\"열한글자가넘는아주긴이름\""
    })
    void 형식이_맞지_않거나_개인정보_동의가_없으면_400(String invalidField) throws Exception {
        String key = invalidField.substring(0, invalidField.indexOf(':'));
        String body = VALID_BODY.replaceFirst(key + ":(\"[^\"]*\"|true)", invalidField);

        mockMvc.perform(post("/api/recruitment/applicants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(applicantService);
    }

    @Test
    void 토큰_없이_로그인할_수_있다() throws Exception {
        given(applicantService.login(any())).willReturn(ApplicantLoginResponse.of("applicant-token"));

        mockMvc.perform(post("/api/recruitment/applicants/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"applicant@cau.ac.kr\",\"password\":\"password1!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("applicant-token"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"email\":\"\",\"password\":\"password1!\"}",
            "{\"email\":\"applicant@cau.ac.kr\"}"
    })
    void 로그인_요청에_이메일이나_비밀번호가_비어_있으면_400(String body) throws Exception {
        mockMvc.perform(post("/api/recruitment/applicants/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(applicantService);
    }

    @Test
    void 지원자_토큰으로_본인_정보를_조회한다() throws Exception {
        given(applicantService.getMe(7L)).willReturn(ApplicantMeResponse.builder()
                .name("김멋사").phone("01012345678").email("applicant@cau.ac.kr").build());
        String token = jwtTokenProvider.createApplicantToken(7L);

        mockMvc.perform(get("/api/recruitment/applicants/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("applicant@cau.ac.kr"));
    }

    @Test
    void 회원_토큰이나_토큰_없이_본인_정보를_조회하면_403() throws Exception {
        String memberToken = jwtTokenProvider.createAccessToken(7L, MemberRole.BABY_LION);

        mockMvc.perform(get("/api/recruitment/applicants/me").header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/recruitment/applicants/me"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(applicantService);
    }
}

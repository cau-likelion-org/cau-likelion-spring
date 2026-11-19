package com.example.cau_likelion_spring.global.config;

import com.example.cau_likelion_spring.global.jwt.JwtAuthenticationFilter;
import com.example.cau_likelion_spring.global.jwt.JwtTokenProvider;
import com.example.cau_likelion_spring.member.controller.MemberController;
import com.example.cau_likelion_spring.member.domain.MemberRole;
import com.example.cau_likelion_spring.member.service.MemberService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 지원자 토큰으로 회원 API(@AuthenticationPrincipal Long memberId)에 접근할 수 없는지 확인 */
@WebMvcTest(controllers = MemberController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtTokenProvider.class})
@TestPropertySource(properties = {
        "jwt.secret=test-secret-key-for-jwt-token-provider-test-0123456789",
        "jwt.access-token-expiration=1h",
        "jwt.refresh-token-expiration=14d",
        "jwt.signup-token-expiration=10m",
        "jwt.applicant-token-expiration=6h",
        "app.frontend-base-url=http://localhost:3000"
})
class SecurityConfigApplicantTokenTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private MemberService memberService;

    /**
     * JwtAuthenticationFilter는 @Component라 Security 체인 밖에서도 서블릿 필터로 한 번 더 등록된다.
     * MockMvc에서는 체인 밖 필터가 먼저 실행되고, OncePerRequestFilter라 체인 안에서는 건너뛰어져 인증이 사라지므로
     * 테스트에서는 체인 밖 등록을 끈다.
     */
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
    void 회원_토큰으로_회원_API에_접근할_수_있다() throws Exception {
        String token = jwtTokenProvider.createAccessToken(3L, MemberRole.BABY_LION);

        mockMvc.perform(get("/api/members/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void 지원자_토큰으로_회원_API에_접근하면_403() throws Exception {
        String token = jwtTokenProvider.createApplicantToken(3L);

        mockMvc.perform(get("/api/members/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void 토큰_없이_회원_API에_접근하면_403() throws Exception {
        mockMvc.perform(get("/api/members/me"))
                .andExpect(status().isForbidden());
    }
}

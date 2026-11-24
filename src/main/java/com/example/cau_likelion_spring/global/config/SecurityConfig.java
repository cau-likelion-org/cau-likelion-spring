package com.example.cau_likelion_spring.global.config;

import com.example.cau_likelion_spring.global.jwt.JwtAuthenticationFilter;
import com.example.cau_likelion_spring.member.domain.MemberRole;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String[] PERMIT_ALL_PATHS = {
            "/api/auth/**",
            "/api/health",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/error"
    };

    /** 회원 API는 회원 역할이 있어야 통과 (지원자 토큰의 ROLE_APPLICANT로는 접근 불가) */
    private static final String[] MEMBER_ROLES = Arrays.stream(MemberRole.values())
            .map(Enum::name)
            .toArray(String[]::new);

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /** 콤마로 구분해서 여러 개(예: 로컬 개발용 + 배포 도메인) 등록 가능 */
    @Value("${app.frontend-base-url}")
    private String frontendBaseUrl;

    /** 리크루팅 지원자 계정 비밀번호 암호화용 (회원은 구글 로그인이라 비밀번호가 없음) */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PERMIT_ALL_PATHS).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/projects/**").permitAll()
                        // /api/blogs/scraping은 숫자 id 패턴에 걸리지 않으므로 아래 permitAll 대상에서 제외됨
                        .requestMatchers(HttpMethod.GET, "/api/blogs", "/api/blogs/{id:[0-9]+}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/gallery/sessions/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/gallery/histories/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/gallery/projects/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/recruitment/subscribers").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/recruitment/subscribers/available-parts").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/recruitment/applications/student-number/check").hasRole("APPLICANT")
                        .requestMatchers(HttpMethod.POST, "/api/recruitment/applications").hasRole("APPLICANT")
                        .requestMatchers(HttpMethod.GET, "/api/recruitment/applications/me").hasRole("APPLICANT")
                        .requestMatchers(HttpMethod.GET, "/api/recruitment/interview-times").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/recruitment/applications/form-options").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/recruitment/questions").permitAll()
                        // 모집공고(일정)는 지원 전 비로그인 방문자도 봐야 함
                        .requestMatchers(HttpMethod.GET, "/api/recruitment/rounds/current").permitAll()
                        // 지원자 로그인 전(계정 생성·비밀번호 재설정)에 쓰는 이메일 인증
                        .requestMatchers(HttpMethod.POST, "/api/recruitment/email-verifications", "/api/recruitment/email-verifications/confirm").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/recruitment/applicants", "/api/recruitment/applicants/login",
                                "/api/recruitment/applicants/password-reset").permitAll()
                        // 지원자 전용 API: anyRequest는 회원 역할만 통과시키므로 지원자 토큰용 규칙을 따로 둔다
                        .requestMatchers(HttpMethod.GET, "/api/recruitment/applicants/me").hasRole("APPLICANT")
                        // 랜딩/소개 페이지에서 비로그인 방문자도 조회해야 하는 admin 관리 콘텐츠 (쓰기는 여전히 인증+권한 필요)
                        .requestMatchers(HttpMethod.GET, "/api/admin/indicator").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/admin/tracks").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/admin/activities").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/admin/faqs").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/admin/desired-talents").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/admin/curriculums").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/admin/roadmap").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/generations").permitAll()
                        .anyRequest().hasAnyRole(MEMBER_ROLES)
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private CorsConfigurationSource corsConfigurationSource() {
        List<String> allowedOrigins = Arrays.stream(frontendBaseUrl.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList();

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE"));
        configuration.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

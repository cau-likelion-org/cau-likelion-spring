package com.example.cau_likelion_spring.global.jwt;

import com.example.cau_likelion_spring.member.domain.MemberRole;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = resolveToken(request);

        if (token != null) {
            try {
                JwtValidationType validationType = jwtTokenProvider.validateToken(token);
                if (validationType == JwtValidationType.VALID_JWT && jwtTokenProvider.isAccessToken(token)) {
                    setAuthentication(token);
                } else {
                    log.info("유효하지 않은 토큰: {}", validationType);
                }
            } catch (Exception e) {
                // 인증 실패 시 요청 자체가 500으로 막히지 않도록, 인증 미설정 상태로 다음 필터로 넘김
                log.error("토큰 인증 처리 중 오류 발생", e);
            }
        }

        filterChain.doFilter(request, response);
    }

    private void setAuthentication(String token) {
        Long memberId = jwtTokenProvider.getMemberId(token);
        MemberRole role = jwtTokenProvider.getRole(token);

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(memberId, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader(AUTHORIZATION_HEADER);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}

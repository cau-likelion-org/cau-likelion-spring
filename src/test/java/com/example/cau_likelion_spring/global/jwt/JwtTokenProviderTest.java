package com.example.cau_likelion_spring.global.jwt;

import com.example.cau_likelion_spring.member.domain.MemberRole;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private final JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(
            "test-secret-key-for-jwt-token-provider-test-0123456789",
            Duration.ofHours(1), Duration.ofDays(14), Duration.ofMinutes(10), Duration.ofHours(6));

    @Test
    void 지원자_토큰은_지원자_토큰으로만_판별되고_지원자_id를_담는다() {
        String token = jwtTokenProvider.createApplicantToken(3L);

        assertThat(jwtTokenProvider.validateToken(token)).isEqualTo(JwtValidationType.VALID_JWT);
        assertThat(jwtTokenProvider.isApplicantToken(token)).isTrue();
        assertThat(jwtTokenProvider.isAccessToken(token)).isFalse();
        assertThat(jwtTokenProvider.getApplicantId(token)).isEqualTo(3L);
        assertThat(jwtTokenProvider.getRole(token)).isNull();
    }

    @Test
    void 회원_access_토큰은_지원자_토큰으로_판별되지_않는다() {
        String token = jwtTokenProvider.createAccessToken(3L, MemberRole.BABY_LION);

        assertThat(jwtTokenProvider.isAccessToken(token)).isTrue();
        assertThat(jwtTokenProvider.isApplicantToken(token)).isFalse();
    }
}

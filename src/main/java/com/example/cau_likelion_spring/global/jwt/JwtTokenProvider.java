package com.example.cau_likelion_spring.global.jwt;

import com.example.cau_likelion_spring.member.domain.MemberRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private static final String ROLE_CLAIM = "role";

    private final SecretKey key;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    public String createAccessToken(Long memberId, MemberRole role) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .claim(ROLE_CLAIM, role.name())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + accessTokenExpiration))
                .signWith(key)
                .compact();
    }

    public String createRefreshToken(Long memberId) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .issuedAt(now)
                .expiration(new Date(now.getTime() + refreshTokenExpiration))
                .signWith(key)
                .compact();
    }

    public long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }

    public Long getMemberId(String token) {
        return Long.valueOf(parseClaims(token).getSubject());
    }

    public MemberRole getRole(String token) {
        String role = parseClaims(token).get(ROLE_CLAIM, String.class);
        return role == null ? null : MemberRole.valueOf(role);
    }

    public JwtValidationType validateToken(String token) {
        try {
            parseClaims(token); // 유효성 검사를 위해 파싱 시도
            return JwtValidationType.VALID_JWT;
        } catch (SignatureException ex) { // 서명이 위조되었거나, 발급 당시와 다른 키로 검증을 시도하는 경우
            return JwtValidationType.INVALID_JWT_SIGNATURE;
        } catch (MalformedJwtException ex) { // 잘못된 형식의 JWT 토큰인 경우 -> 헤더.페이로드.서명 형식이 아니거나 토큰 문자열이 손상되었을 때
            return JwtValidationType.INVALID_JWT_TOKEN;
        } catch (ExpiredJwtException ex) { // 토큰의 유효 기간이 만료된 경우 -> 보통 해당 에러가 날 경우 reissue 처리하는 경우도 있습니다
            return JwtValidationType.EXPIRED_JWT_TOKEN;
        } catch (UnsupportedJwtException ex) { // 지원하지 않는 JWT 토큰이 전달되는 경우 -> 다른 서명 알고리즘이 사용된 JWT가 전달될 경우
            return JwtValidationType.UNSUPPORTED_JWT_TOKEN;
        } catch (IllegalArgumentException ex) { // 빈 문자열 같이 유효하지 않은 문자열로 JWT 파싱이 시도되는 경우
            return JwtValidationType.EMPTY_JWT;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

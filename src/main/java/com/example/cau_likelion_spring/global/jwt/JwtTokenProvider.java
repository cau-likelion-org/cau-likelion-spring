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
import java.time.Duration;
import java.util.Date;
import java.util.Map;

@Component
public class JwtTokenProvider {

    private static final String ROLE_CLAIM = "role";
    private static final String TYPE_CLAIM = "type";

    private enum TokenType {
        ACCESS, REFRESH, SIGNUP
    }

    private final SecretKey key;
    private final Duration accessTokenExpiration;
    private final Duration refreshTokenExpiration;
    private final Duration signupTokenExpiration;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration}") Duration accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration}") Duration refreshTokenExpiration,
            @Value("${jwt.signup-token-expiration}") Duration signupTokenExpiration
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
        this.signupTokenExpiration = signupTokenExpiration;
    }

    public String createAccessToken(Long memberId, MemberRole role) {
        return buildToken(String.valueOf(memberId), Map.of(ROLE_CLAIM, role.name(), TYPE_CLAIM, TokenType.ACCESS.name()), accessTokenExpiration);
    }

    public String createRefreshToken(Long memberId) {
        return buildToken(String.valueOf(memberId), Map.of(TYPE_CLAIM, TokenType.REFRESH.name()), refreshTokenExpiration);
    }

    /**
     * 구글 로그인 성공~회원가입 폼 제출 사이에만 쓰는 단명 토큰.
     * subject는 구글이 검증해 준 이메일이고, join() 시점에 이 이메일을 다시 꺼내 쓴다.
     */
    public String createSignupToken(String email) {
        return buildToken(email, Map.of(TYPE_CLAIM, TokenType.SIGNUP.name()), signupTokenExpiration);
    }

    private String buildToken(String subject, Map<String, ?> claims, Duration expiration) {
        Date now = new Date();
        return Jwts.builder()
                .subject(subject)
                .claims(claims)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiration.toMillis()))
                .signWith(key)
                .compact();
    }

    public Duration getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }

    public Long getMemberId(String token) {
        return Long.valueOf(parseClaims(token).getSubject());
    }

    public MemberRole getRole(String token) {
        String role = parseClaims(token).get(ROLE_CLAIM, String.class);
        return role == null ? null : MemberRole.valueOf(role);
    }

    public String getEmail(String token) {
        return parseClaims(token).getSubject();
    }

    /** signupToken 자리에 access/refresh 토큰이 잘못 들어온 경우를 걸러내기 위한 타입 체크 */
    public boolean isSignupToken(String token) {
        return isTokenType(token, TokenType.SIGNUP);
    }

    /** API 인증(Authorization 헤더) 자리에 signup/refresh 토큰이 잘못 들어온 경우를 걸러내기 위한 타입 체크 */
    public boolean isAccessToken(String token) {
        return isTokenType(token, TokenType.ACCESS);
    }

    private boolean isTokenType(String token, TokenType type) {
        return type.name().equals(parseClaims(token).get(TYPE_CLAIM, String.class));
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

package com.example.cau_likelion_spring.auth;

import com.example.cau_likelion_spring.global.exception.CustomException;
import com.example.cau_likelion_spring.global.exception.ErrorCode;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;

/** 프론트가 구글 로그인으로 받아온 ID Token이 실제로 구글이 발급한 게 맞는지 검증한다. */
@Component
public class GoogleTokenVerifier {

    private final GoogleIdTokenVerifier verifier;

    public GoogleTokenVerifier(@Value("${google.client-id}") String clientId) {
        this.verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(clientId))
                .build();
    }

    /** 서명·만료·발급자(aud/iss)를 검증하고, 이메일 인증까지 확인된 이메일을 반환한다. */
    public String verifyAndGetEmail(String idTokenString) {
        GoogleIdToken idToken;
        try {
            idToken = verifier.verify(idTokenString);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INVALID_TOKEN, "구글 로그인 토큰 검증에 실패했습니다.");
        }

        if (idToken == null) {
            throw new CustomException(ErrorCode.INVALID_TOKEN, "유효하지 않거나 만료된 구글 로그인 토큰입니다.");
        }

        GoogleIdToken.Payload payload = idToken.getPayload();
        if (!Boolean.TRUE.equals(payload.getEmailVerified())) {
            throw new CustomException(ErrorCode.INVALID_TOKEN, "이메일이 인증되지 않은 구글 계정입니다.");
        }

        return payload.getEmail();
    }
}

package com.example.cau_likelion_spring.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.util.Base64;

/**
 * FIREBASE_CREDENTIALS_BASE64가 비어있으면(아직 Firebase 프로젝트 준비 전) FirebaseApp을 초기화하지 않는다.
 * 이 경우 푸시 알림 발송 기능만 동작하지 않고 나머지 기능에는 영향 없다 (mail.password 빈 값 처리와 동일한 방식).
 */
@Slf4j
@Configuration
public class FirebaseConfig {

    private final String credentialsBase64;

    public FirebaseConfig(@Value("${firebase.credentials-base64:}") String credentialsBase64) {
        this.credentialsBase64 = credentialsBase64;
    }

    @Bean
    public FirebaseApp firebaseApp() throws Exception {
        if (!StringUtils.hasText(credentialsBase64)) {
            log.warn("FIREBASE_CREDENTIALS_BASE64가 설정되지 않아 FCM 푸시 알림 기능이 비활성화됩니다.");
            return null;
        }

        byte[] decoded = Base64.getDecoder().decode(credentialsBase64);
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(new ByteArrayInputStream(decoded)))
                .build();

        return FirebaseApp.getApps().isEmpty() ? FirebaseApp.initializeApp(options) : FirebaseApp.getApps().get(0);
    }
}

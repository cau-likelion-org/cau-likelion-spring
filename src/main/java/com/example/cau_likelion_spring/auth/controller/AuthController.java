package com.example.cau_likelion_spring.auth.controller;

import com.example.cau_likelion_spring.auth.dto.LoginRequest;
import com.example.cau_likelion_spring.auth.dto.RefreshTokenRequest;
import com.example.cau_likelion_spring.auth.dto.JoinRequest;
import com.example.cau_likelion_spring.auth.dto.TokenResponse;
import com.example.cau_likelion_spring.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "회원가입/로그인/토큰 재발급/로그아웃 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "회원가입", description = "사전 등록된 이메일(AllowedUserEmail)과 기수가 일치하는 경우에만 가입할 수 있습니다. 가입 시 role은 무조건 BABY_LION으로 생성됩니다.")
    @PostMapping("/join")
    public ResponseEntity<TokenResponse> join(@Valid @RequestBody JoinRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.join(request));
    }

    @Operation(summary = "로그인", description = "이미 가입된 이메일로 로그인합니다. (현재는 구글 OAuth 연동 전이라 이메일만으로 로그인됩니다.)")
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(summary = "Access Token 재발급", description = "유효한 Refresh Token으로 Access Token을 재발급합니다. Refresh Token은 로테이션되지 않습니다.")
    @PostMapping("/reissue")
    public ResponseEntity<TokenResponse> reissue(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.reissue(request));
    }

    @Operation(summary = "로그아웃", description = "전달받은 Refresh Token을 삭제합니다.")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request);
        return ResponseEntity.noContent().build();
    }
}

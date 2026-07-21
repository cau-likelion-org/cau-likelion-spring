package com.example.cau_likelion_spring.auth.controller;

import com.example.cau_likelion_spring.auth.dto.LoginRequest;
import com.example.cau_likelion_spring.auth.dto.RefreshTokenRequest;
import com.example.cau_likelion_spring.auth.dto.JoinRequest;
import com.example.cau_likelion_spring.auth.dto.TokenResponse;
import com.example.cau_likelion_spring.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/join")
    public ResponseEntity<TokenResponse> join(@Valid @RequestBody JoinRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.join(request));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/reissue")
    public ResponseEntity<TokenResponse> reissue(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.reissue(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request);
        return ResponseEntity.noContent().build();
    }
}

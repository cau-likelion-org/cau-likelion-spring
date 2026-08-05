package com.example.cau_likelion_spring.auth.controller;

import com.example.cau_likelion_spring.auth.GoogleTokenVerifier;
import com.example.cau_likelion_spring.auth.dto.GoogleLoginRequest;
import com.example.cau_likelion_spring.auth.dto.GoogleLoginResponse;
import com.example.cau_likelion_spring.auth.dto.OAuthLoginResult;
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
    private final GoogleTokenVerifier googleTokenVerifier;

    @Operation(summary = "구글 로그인", description = "프론트에서 Google Identity Services로 받은 ID Token을 검증합니다. "
            + "이미 가입된 이메일이면 바로 로그인 토큰을 발급하고(status=LOGIN_SUCCESS), "
            + "사전 등록된 신규 이메일이면 가입 폼으로 넘길 signupToken을 발급합니다(status=SIGNUP_REQUIRED). "
            + "사전 등록되지 않은 이메일이면 403(EMAIL_NOT_ALLOWED)이 반환됩니다.")
    @PostMapping("/google-login")
    public ResponseEntity<GoogleLoginResponse> googleLogin(@Valid @RequestBody GoogleLoginRequest request) {
        String email = googleTokenVerifier.verifyAndGetEmail(request.idToken());
        OAuthLoginResult result = authService.processOAuthLogin(email);
        return ResponseEntity.ok(toResponse(result));
    }

    private GoogleLoginResponse toResponse(OAuthLoginResult result) {
        if (result instanceof OAuthLoginResult.LoginSuccess success) {
            return GoogleLoginResponse.loginSuccess(success.tokens());
        }
        OAuthLoginResult.SignupRequired signupRequired = (OAuthLoginResult.SignupRequired) result;
        return GoogleLoginResponse.signupRequired(signupRequired.signupToken());
    }

    @Operation(summary = "회원가입", description = "구글 로그인 성공 시 발급된 signupToken(가입용 임시 토큰)에서 이메일을 꺼내, "
            + "사전 등록된 이메일(AllowedUserEmail)과 기수가 일치하는 경우에만 가입할 수 있습니다. 가입 시 role은 무조건 BABY_LION으로 생성됩니다.")
    @PostMapping("/join")
    public ResponseEntity<TokenResponse> join(@Valid @RequestBody JoinRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.join(request));
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

package com.eeerrorcode.lottomate.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eeerrorcode.lottomate.domain.ApiResponse;
import com.eeerrorcode.lottomate.domain.dto.user.AuthResponse;
import com.eeerrorcode.lottomate.domain.dto.user.LoginRequest;
import com.eeerrorcode.lottomate.domain.dto.user.SignupRequest;
import com.eeerrorcode.lottomate.domain.dto.user.SocialLoginRequest;
import com.eeerrorcode.lottomate.security.AuthService;
import com.eeerrorcode.lottomate.security.OAuth2Service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;  // 추가

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j  // 추가
public class AuthController {

    private final AuthService authService;
    private final OAuth2Service oAuth2Service;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse> register(@RequestBody @Valid SignupRequest request) {
        authService.register(request);
        return ResponseEntity.ok(new ApiResponse("success", "회원가입 성공"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@RequestBody @Valid LoginRequest request) {
        AuthResponse authResponse = authService.login(request);

        ApiResponse response = new ApiResponse("success", "로그인 성공");
        response.setData(authResponse);

        return ResponseEntity.ok(response);
    }

    /**
     * 소셜 로그인 처리
     */
    @PostMapping("/social-login")
    public ResponseEntity<ApiResponse> socialLogin(@RequestBody @Valid SocialLoginRequest request) {
        try {
            log.info("소셜 로그인 요청 - 제공자: {}", request.getProvider());
            AuthResponse authResponse = oAuth2Service.processSocialLogin(request);
            ApiResponse response = new ApiResponse("success", "소셜 로그인 성공");
            response.setData(authResponse);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("소셜 로그인 오류", e);
            ApiResponse errorResponse = new ApiResponse("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
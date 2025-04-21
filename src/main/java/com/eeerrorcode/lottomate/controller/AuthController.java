package com.eeerrorcode.lottomate.controller;

import java.io.IOException;  // 추가

import org.springframework.beans.factory.annotation.Value;  // 추가
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eeerrorcode.lottomate.domain.ApiResponse;
import com.eeerrorcode.lottomate.domain.dto.user.AuthResponse;
import com.eeerrorcode.lottomate.domain.dto.user.LoginRequest;
import com.eeerrorcode.lottomate.domain.dto.user.SignupRequest;
import com.eeerrorcode.lottomate.domain.dto.user.SocialLoginRequest;
import com.eeerrorcode.lottomate.domain.entity.user.SocialAccount.Provider;  // 추가
import com.eeerrorcode.lottomate.security.AuthService;
import com.eeerrorcode.lottomate.security.OAuth2Service;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final OAuth2Service oAuth2Service;
    
    @Value("${frontend.url}")  // 추가: application.yml에 frontend.url 속성 추가 필요
    private String frontEndUrl;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse> register(@RequestBody @Valid SignupRequest request) {
        authService.register(request);
        return ResponseEntity.ok(new ApiResponse("success", "회원가입 성공"));
    }

    @GetMapping("/oauth2/authorize/{provider}")
    public ResponseEntity<Void> authorizeOAuth2(@PathVariable String provider, 
                                            HttpServletResponse response) throws IOException {
        try {
            // 대소문자 무관하게 enum으로 변환
            Provider providerEnum = Provider.valueOf(provider.toUpperCase());
            String authorizationUrl = oAuth2Service.getAuthorizationUrl(providerEnum);
            response.sendRedirect(authorizationUrl);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            // 잘못된 Provider 처리
            throw new IllegalArgumentException("지원하지 않는 소셜 로그인 제공자: " + provider);
        }
    }

    @GetMapping("/oauth2/callback/{provider}")
    public ResponseEntity<Void> oauth2Callback(@PathVariable String provider,
                                            @RequestParam("code") String code,
                                            HttpServletResponse response) throws IOException {
        try {
            Provider providerEnum = Provider.valueOf(provider.toUpperCase());
            AuthResponse authResponse = oAuth2Service.processOAuth2Callback(providerEnum, code);
            
            // 프론트엔드로 리다이렉트 (토큰을 URL 파라미터로 전달하거나 쿠키에 설정)
            String redirectUrl = frontEndUrl + "?token=" + authResponse.getAccessToken() +
                                "&refreshToken=" + authResponse.getRefreshToken();
            response.sendRedirect(redirectUrl);
            
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("지원하지 않는 소셜 로그인 제공자: " + provider);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@RequestBody @Valid LoginRequest request) {
        AuthResponse authResponse = authService.login(request);

        ApiResponse response = new ApiResponse("success", "로그인 성공");
        response.setData(authResponse);

        return ResponseEntity.ok(response);
    }

    /**
     * 소셜 로그인 처리 (클라이언트에서 토큰을 받은 경우)
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
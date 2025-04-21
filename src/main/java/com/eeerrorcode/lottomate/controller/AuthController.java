package com.eeerrorcode.lottomate.controller;

import java.io.IOException;

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
import com.eeerrorcode.lottomate.domain.entity.user.SocialAccount.Provider;
import com.eeerrorcode.lottomate.security.AuthService;
import com.eeerrorcode.lottomate.security.OAuth2Service;

import jakarta.servlet.http.HttpServletRequest;
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

    /**
     * 현재 환경에 맞는 프론트엔드 URL을 결정합니다.
     * 1. 서버 호스트명을 기준으로 환경 판단
     * 2. 환경에 따라 적절한 프론트엔드 URL 반환
     */
    private String getFrontendUrl(HttpServletRequest request) {
        String serverName = request.getServerName();
        // 프로덕션 환경
        if (serverName.contains("lottomateapi.eeerrorcode.com")) {
            return "https://lottomate.eeerrorcode.com";
        }
        // 로컬 환경 확인
        return "http://localhost:3000";

    }

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
                                            HttpServletRequest request,
                                            HttpServletResponse response) throws IOException {
        try {
            Provider providerEnum = Provider.valueOf(provider.toUpperCase());
            AuthResponse authResponse = oAuth2Service.processOAuth2Callback(providerEnum, code);
            
            // 현재 환경에 맞는 프론트엔드 URL 결정
            String frontendUrl = getFrontendUrl(request);
            log.info("소셜 로그인 성공, 프론트엔드 리다이렉트: {}", frontendUrl);
            
            // 프론트엔드로 리다이렉트 (토큰을 URL 파라미터로 전달)
            String redirectUrl = frontendUrl + 
                                "/oauth/callback?token=" + authResponse.getAccessToken() +
                                "&refreshToken=" + authResponse.getRefreshToken();
            
            response.sendRedirect(redirectUrl);
            
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.error("OAuth 콜백 처리 중 오류 발생", e);
            throw new IllegalArgumentException("지원하지 않는 소셜 로그인 제공자: " + provider);
        } catch (Exception e) {
            log.error("OAuth 콜백 처리 중 예상치 못한 오류 발생", e);
            // 오류 페이지로 리다이렉트 (프론트엔드에 오류 페이지 필요)
            response.sendRedirect(getFrontendUrl(request) + "/login?error=auth_failed");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
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
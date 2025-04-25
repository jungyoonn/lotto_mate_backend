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

import com.eeerrorcode.lottomate.domain.dto.CommonResponse;
import com.eeerrorcode.lottomate.domain.dto.user.AuthResponse;
import com.eeerrorcode.lottomate.domain.dto.user.LoginRequest;
import com.eeerrorcode.lottomate.domain.dto.user.SignupRequest;
import com.eeerrorcode.lottomate.domain.dto.user.SocialLoginRequest;
import com.eeerrorcode.lottomate.domain.dto.user.UnifiedLoginRequest;
import com.eeerrorcode.lottomate.domain.dto.user.UnifiedLoginRequest.LoginType;
import com.eeerrorcode.lottomate.domain.entity.user.SocialAccount.Provider;
import com.eeerrorcode.lottomate.exeption.AuthenticationException;
import com.eeerrorcode.lottomate.exeption.RegistrationException;
import com.eeerrorcode.lottomate.security.AuthService;
import com.eeerrorcode.lottomate.security.OAuth2Service;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Log4j2
@Tag(name = "Authentication API", description = "인증 관련 기능을 제공하는 API입니다")
public class AuthController {

    private final AuthService authService;
    private final OAuth2Service oAuth2Service;

    /**
     * 현재 환경에 맞는 프론트엔드 URL을 결정합니다.
     */
    private String getFrontendUrl(HttpServletRequest request) {
        String serverName = request.getServerName();
        // 프로덕션 환경
        if (serverName.contains("lottomateapi.eeerrorcode.com")) {
            return "https://lottomate.eeerrorcode.com";
        }
        // 로컬 환경 또는 기타 환경
        return "http://localhost:3000";
    }

    @Operation(
        summary = "회원가입",
        description = "이메일, 비밀번호, 이름 등으로 일반 회원가입을 처리합니다",
        responses = {
            @ApiResponse(
                responseCode = "200", 
                description = "회원가입 성공", 
                content = @Content(schema = @Schema(implementation = CommonResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 요청")
        }
    )
    @PostMapping("/signup")
    public ResponseEntity<CommonResponse<Void>> register(@RequestBody @Valid SignupRequest request) {
        try {
            authService.register(request);
            return ResponseEntity.ok(CommonResponse.success(null, "회원가입이 성공적으로 완료되었습니다"));
        } catch (RegistrationException e) {
            // 이메일 중복 등 등록 관련 예외 발생 시
            log.warn("회원가입 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(CommonResponse.error("REGISTRATION_ERROR", e.getMessage()));
        } catch (Exception e) {
            log.error("회원가입 처리 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CommonResponse.error("SERVER_ERROR", "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요."));
        }
    }

    @Operation(
        summary = "소셜 로그인 인증 URL 요청",
        description = "특정 소셜 로그인 제공자(Google, Kakao 등)의 인증 URL로 리다이렉트합니다",
        responses = {
            @ApiResponse(responseCode = "302", description = "소셜 로그인 페이지로 리다이렉트"),
            @ApiResponse(responseCode = "400", description = "잘못된 소셜 로그인 제공자")
        }
    )
    @GetMapping("/oauth2/authorize/{provider}")
    public ResponseEntity<Void> authorizeOAuth2(
            @PathVariable("provider") String provider, 
            HttpServletResponse response) throws IOException {
        try {
            // 대소문자 무관하게 enum으로 변환
            Provider providerEnum = Provider.valueOf(provider.toUpperCase());
            String authorizationUrl = oAuth2Service.getAuthorizationUrl(providerEnum);
            
            log.info("소셜 로그인 인증 URL 요청: provider={}, url={}", provider, authorizationUrl);
            response.sendRedirect(authorizationUrl);
            
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.error("지원하지 않는 소셜 로그인 제공자: {}", provider);
            throw new IllegalArgumentException("지원하지 않는 소셜 로그인 제공자: " + provider);
        }
    }

    @Operation(
        summary = "소셜 로그인 콜백 처리",
        description = "소셜 로그인 제공자로부터 받은 인증 코드를 처리하고 JWT 토큰을 발급합니다",
        responses = {
            @ApiResponse(responseCode = "302", description = "프론트엔드로 토큰과 함께 리다이렉트"),
            @ApiResponse(responseCode = "400", description = "잘못된 소셜 로그인 제공자"),
            @ApiResponse(responseCode = "500", description = "인증 처리 중 오류 발생")
        }
    )
    @GetMapping("/oauth2/callback/{provider}")
    public ResponseEntity<Void> oauth2Callback(
            @PathVariable("provider") String provider,
            @RequestParam("code") String code,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        try {
            Provider providerEnum = Provider.valueOf(provider.toUpperCase());
            AuthResponse authResponse = oAuth2Service.processOAuth2Callback(providerEnum, code);
            
            // 현재 환경에 맞는 프론트엔드 URL 결정
            String frontendUrl = getFrontendUrl(request);
            log.info("소셜 로그인 성공, 프론트엔드 리다이렉트: provider={}, frontend={}", provider, frontendUrl);
            
            // 프론트엔드로 리다이렉트 (토큰을 URL 파라미터로 전달)
            String redirectUrl = frontendUrl + 
                                "/oauth/callback?token=" + authResponse.getAccessToken() +
                                "&refreshToken=" + authResponse.getRefreshToken();
            
            response.sendRedirect(redirectUrl);
            
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.error("OAuth 콜백 처리 중 오류 발생: {}", e.getMessage(), e);
            throw new IllegalArgumentException("지원하지 않는 소셜 로그인 제공자: " + provider);
        } catch (Exception e) {
            log.error("OAuth 콜백 처리 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
            // 오류 페이지로 리다이렉트 (프론트엔드에 오류 페이지 필요)
            response.sendRedirect(getFrontendUrl(request) + "/login?error=auth_failed");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(
        summary = "일반 로그인",
        description = "이메일과 비밀번호로 로그인하고 JWT 토큰을 발급받습니다",
        responses = {
            @ApiResponse(
                responseCode = "200", 
                description = "로그인 성공", 
                content = @Content(schema = @Schema(implementation = CommonResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
        }
    )
    @PostMapping("/login")
    public ResponseEntity<CommonResponse<AuthResponse>> login(@RequestBody @Valid LoginRequest request) {
        try {
            AuthResponse authResponse = authService.login(request);
            return ResponseEntity.ok(CommonResponse.success(authResponse, "로그인 성공"));
        } catch (IllegalArgumentException e) {
            log.warn("로그인 실패: {}", e.getMessage());
            throw new AuthenticationException(e.getMessage());
        }
    }

    @Operation(
        summary = "소셜 로그인 (토큰 기반)",
        description = "클라이언트에서 받은 소셜 로그인 토큰으로 인증 후 JWT 토큰을 발급받습니다",
        responses = {
            @ApiResponse(
                responseCode = "200", 
                description = "소셜 로그인 성공", 
                content = @Content(schema = @Schema(implementation = CommonResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 요청"),
            @ApiResponse(responseCode = "500", description = "소셜 로그인 처리 중 오류 발생")
        }
    )
    @PostMapping("/social-login")
    public ResponseEntity<CommonResponse<AuthResponse>> socialLogin(@RequestBody @Valid SocialLoginRequest request) {
        try {
            log.info("소셜 로그인 요청 - 제공자: {}", request.getProvider());
            AuthResponse authResponse = oAuth2Service.processSocialLogin(request);
            return ResponseEntity.ok(CommonResponse.success(authResponse, "소셜 로그인 성공"));
        } catch (Exception e) {
            log.error("소셜 로그인 오류: {}", e.getMessage(), e);
            throw new AuthenticationException("소셜 로그인 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 통합 로그인 엔드포인트
     * 일반 로그인과 소셜 로그인을 하나의 엔드포인트로 처리
     */
    @Operation(
        summary = "통합 로그인",
        description = "이메일/비밀번호 또는 소셜 토큰으로 로그인합니다",
        responses = {
            @ApiResponse(
                responseCode = "200", 
                description = "로그인 성공", 
                content = @Content(schema = @Schema(implementation = CommonResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
        }
    )
    @PostMapping("/unified-login")
    public ResponseEntity<CommonResponse<AuthResponse>> unifiedLogin(@RequestBody @Valid UnifiedLoginRequest request) {
        try {
            AuthResponse authResponse;
            
            // 로그인 유형에 따라 처리를 분기
            if (request.getLoginType() == LoginType.EMAIL_PASSWORD) {
                // 일반 로그인
                LoginRequest loginRequest = new LoginRequest();
                loginRequest.setEmail(request.getEmail());
                loginRequest.setPassword(request.getPassword());
                
                authResponse = authService.login(loginRequest);
            } else if (request.getLoginType() == LoginType.SOCIAL) {
                // 소셜 로그인
                SocialLoginRequest socialRequest = new SocialLoginRequest();
                socialRequest.setProvider(request.getProvider());
                socialRequest.setToken(request.getSocialToken());
                
                authResponse = oAuth2Service.processSocialLogin(socialRequest);
            } else {
                return ResponseEntity.badRequest()
                    .body(CommonResponse.error("INVALID_LOGIN_TYPE", "지원하지 않는 로그인 유형입니다."));
            }
            
            return ResponseEntity.ok(CommonResponse.success(authResponse, "통합 로그인 성공"));
        } catch (IllegalArgumentException | AuthenticationException e) {
            log.warn("로그인 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(CommonResponse.error("AUTH_FAILED", e.getMessage()));
        } catch (Exception e) {
            log.error("로그인 처리 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CommonResponse.error("SERVER_ERROR", "서버 오류가 발생했습니다."));
        }
    }
}
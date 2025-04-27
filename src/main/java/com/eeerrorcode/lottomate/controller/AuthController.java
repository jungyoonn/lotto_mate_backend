package com.eeerrorcode.lottomate.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
import com.eeerrorcode.lottomate.domain.dto.user.LogoutRequest;
import com.eeerrorcode.lottomate.domain.dto.user.OAuth2UserInfo;
import com.eeerrorcode.lottomate.domain.dto.user.RefreshTokenDto;
import com.eeerrorcode.lottomate.domain.dto.user.SignupRequest;
import com.eeerrorcode.lottomate.domain.dto.user.SocialLoginRequest;
import com.eeerrorcode.lottomate.domain.dto.user.TokenRefreshRequest;
import com.eeerrorcode.lottomate.domain.dto.user.UnifiedLoginRequest;
import com.eeerrorcode.lottomate.domain.dto.user.UnifiedLoginRequest.LoginType;
import com.eeerrorcode.lottomate.domain.entity.user.SocialAccount;
import com.eeerrorcode.lottomate.domain.entity.user.SocialAccount.Provider;
import com.eeerrorcode.lottomate.domain.entity.user.User;
import com.eeerrorcode.lottomate.domain.entity.user.User.Role;
import com.eeerrorcode.lottomate.exeption.AuthenticationException;
import com.eeerrorcode.lottomate.exeption.RegistrationException;
import com.eeerrorcode.lottomate.repository.SocialAccountRepository;
import com.eeerrorcode.lottomate.repository.UserRepository;
import com.eeerrorcode.lottomate.security.AuthService;
import com.eeerrorcode.lottomate.security.CustomUserDetails;
import com.eeerrorcode.lottomate.security.OAuth2Service;
import com.eeerrorcode.lottomate.service.user.RefreshTokenService;
import com.eeerrorcode.lottomate.service.user.RefreshTokenService.TokenRefreshResponseDto;

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
    private final UserRepository userRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final RefreshTokenService refreshTokenService;

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
    public ResponseEntity<CommonResponse<AuthResponse>> unifiedLogin(
            @RequestBody @Valid UnifiedLoginRequest request,
            HttpServletRequest httpRequest) {
        try {
            // 디바이스 정보 추출 (User-Agent 또는 클라이언트에서 전달한 값)
            String deviceInfo = request.getDeviceInfo();
            if (deviceInfo == null || deviceInfo.isEmpty()) {
                deviceInfo = httpRequest.getHeader("User-Agent");
                if (deviceInfo == null) {
                    deviceInfo = "UNKNOWN";
                }
            }
            
            User user = null;
            
            // 로그인 유형에 따라 처리를 분기
            if (request.getLoginType() == LoginType.EMAIL_PASSWORD) {
                // 일반 로그인
                user = userRepository.findByEmail(request.getEmail())
                        .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));
                
                if (!authService.verifyPassword(request.getPassword(), user.getPassword())) {
                    throw new IllegalArgumentException("비밀번호가 틀렸습니다.");
                }
            } else if (request.getLoginType() == LoginType.SOCIAL) {
                // 소셜 로그인
                OAuth2UserInfo userInfo = oAuth2Service.getUserInfo(request.getProvider(), request.getSocialToken());
                
                // 소셜 계정 검색
                Optional<SocialAccount> existingSocialAccount = 
                        socialAccountRepository.findByProviderAndSocialId(request.getProvider(), userInfo.getId());
                
                if (existingSocialAccount.isPresent()) {
                    user = existingSocialAccount.get().getUser();
                } else {
                    // 이메일로 사용자 찾기 시도
                    Optional<User> existingUser = userRepository.findByEmail(userInfo.getEmail());
                    
                    if (existingUser.isPresent()) {
                        user = existingUser.get();
                        // 소셜 계정 연결
                        createSocialAccount(user, userInfo);
                    } else {
                        // 새 사용자 생성
                        user = createUserFromOAuth2(userInfo);
                    }
                }
            } else {
                return ResponseEntity.badRequest()
                    .body(CommonResponse.error("INVALID_LOGIN_TYPE", "지원하지 않는 로그인 유형입니다."));
            }
            
            // 공통 인증 처리 로직 - 액세스 토큰과 리프레시 토큰 생성
            AuthResponse authResponse = authService.processUnifiedLoginResponse(user, deviceInfo);
            
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
    
    @Operation(
        summary = "토큰 갱신",
        description = "리프레시 토큰을 사용하여 새 엑세스 토큰을 발급합니다",
        responses = {
            @ApiResponse(
                responseCode = "200", 
                description = "토큰 갱신 성공", 
                content = @Content(schema = @Schema(implementation = CommonResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 리프레시 토큰")
        }
    )
    @PostMapping("/refresh-token")
    public ResponseEntity<CommonResponse<?>> refreshToken(@RequestBody @Valid TokenRefreshRequest request) {
        try {
            TokenRefreshResponseDto tokenResponse = refreshTokenService.refreshToken(request.getRefreshToken());
            return ResponseEntity.ok(CommonResponse.success(tokenResponse, "토큰이 성공적으로 갱신되었습니다"));
        } catch (AuthenticationException e) {
            log.warn("토큰 갱신 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(CommonResponse.error("INVALID_REFRESH_TOKEN", e.getMessage()));
        } catch (Exception e) {
            log.error("토큰 갱신 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CommonResponse.error("SERVER_ERROR", "서버 오류가 발생했습니다."));
        }
    }

    @Operation(
        summary = "로그아웃",
        description = "사용자의 리프레시 토큰을 무효화하여 로그아웃 처리합니다",
        responses = {
            @ApiResponse(
                responseCode = "200", 
                description = "로그아웃 성공", 
                content = @Content(schema = @Schema(implementation = CommonResponse.class))
            )
        }
    )
    @PostMapping("/logout")
    public ResponseEntity<CommonResponse<Void>> logout(
            @RequestBody LogoutRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        if (userDetails != null && userDetails instanceof CustomUserDetails) {
            Long userId = ((CustomUserDetails) userDetails).getUser().getId();
            
            if (request.isLogoutAll()) {
                // 모든 기기에서 로그아웃
                refreshTokenService.logoutAll(userId);
                return ResponseEntity.ok(CommonResponse.success(null, "모든 기기에서 로그아웃되었습니다"));
            } else if (request.getRefreshToken() != null && !request.getRefreshToken().isEmpty()) {
                // 현재 세션만 로그아웃
                refreshTokenService.logout(request.getRefreshToken());
                return ResponseEntity.ok(CommonResponse.success(null, "로그아웃되었습니다"));
            }
        }
        
        // 로그인되지 않은 상태에서의 요청 처리
        return ResponseEntity.ok(CommonResponse.success(null, "로그아웃 처리되었습니다"));
    }
    
    @Operation(
        summary = "활성 세션 조회",
        description = "사용자의 현재 활성 로그인 세션 목록을 조회합니다",
        responses = {
            @ApiResponse(
                responseCode = "200", 
                description = "세션 조회 성공", 
                content = @Content(schema = @Schema(implementation = CommonResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "인증 실패")
        }
    )
    @GetMapping("/sessions")
    public ResponseEntity<CommonResponse<List<RefreshTokenDto>>> getSessions(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        if (userDetails == null || !(userDetails instanceof CustomUserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(CommonResponse.error("UNAUTHORIZED", "인증이 필요합니다"));
        }
        
        Long userId = ((CustomUserDetails) userDetails).getUser().getId();
        List<RefreshTokenDto> sessions = refreshTokenService.getRefreshTokensByUserId(userId);
        
        return ResponseEntity.ok(CommonResponse.success(sessions, "활성 세션 조회 성공"));
    }
    
    /**
     * OAuth2 정보로 새로운 User 엔티티 생성
     */
    private User createUserFromOAuth2(OAuth2UserInfo userInfo) {
        // 이메일이 null인 경우 대체 이메일 생성
        String email = userInfo.getEmail();
        if (email == null) {
            // 소셜 ID와 제공자를 조합하여 가상 이메일 생성
            email = userInfo.getId() + "@" + userInfo.getProvider().toString().toLowerCase() + ".user";
        }
        
        User user = User.builder()
                .email(email)
                .name(userInfo.getName())
                .profileImage(userInfo.getProfileImage())
                .password(null)  // 소셜 로그인 사용자는 비밀번호가 없음
                .role(Role.USER)
                .isActive(true)
                .emailVerified(true)  // 소셜 로그인은 이메일이 이미 확인됨
                .build();
        
        return userRepository.save(user);
    }
    
    /**
     * OAuth2 정보로 SocialAccount 엔티티 생성
     */
    private SocialAccount createSocialAccount(User user, OAuth2UserInfo userInfo) {
        SocialAccount socialAccount = SocialAccount.builder()
                .user(user)
                .provider(userInfo.getProvider())
                .socialId(userInfo.getId())
                .socialEmail(userInfo.getEmail())
                .socialName(userInfo.getName())
                .socialProfileImage(userInfo.getProfileImage())
                .accessToken(userInfo.getAccessToken())
                .refreshToken(userInfo.getRefreshToken())
                .tokenExpiresAt(LocalDateTime.now().plusDays(60))  // 토큰 만료 시간은 필요에 따라 조정
                .build();
        
        return socialAccountRepository.save(socialAccount);
    }
}
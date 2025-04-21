package com.eeerrorcode.lottomate.security;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;  // 수정된 import
import org.springframework.stereotype.Service;

import com.eeerrorcode.lottomate.domain.dto.user.AuthResponse;
import com.eeerrorcode.lottomate.domain.dto.user.OAuth2TokenResponse;
import com.eeerrorcode.lottomate.domain.dto.user.OAuth2UserInfo;
import com.eeerrorcode.lottomate.domain.dto.user.SocialLoginRequest;
import com.eeerrorcode.lottomate.domain.entity.user.SocialAccount;
import com.eeerrorcode.lottomate.domain.entity.user.SocialAccount.Provider;
import com.eeerrorcode.lottomate.domain.entity.user.User;
import com.eeerrorcode.lottomate.domain.entity.user.User.Role;
import com.eeerrorcode.lottomate.repository.SocialAccountRepository;
import com.eeerrorcode.lottomate.repository.UserRepository;
import com.eeerrorcode.lottomate.security.exception.OAuth2AuthenticationException;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OAuth2Service {

    private final UserRepository userRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final OAuth2ClientService oAuth2ClientService;
    
    @Value("${oauth2.client.registration.google.client-id}")
    private String googleClientId;
    
    @Value("${oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;
    
    @Value("${oauth2.client.registration.google.redirect-uri}")
    private String googleRedirectUri;
    
    @Value("${oauth2.client.registration.kakao.client-id}")
    private String kakaoClientId;
    
    @Value("${oauth2.client.registration.kakao.client-secret}")
    private String kakaoClientSecret;
    
    @Value("${oauth2.client.registration.kakao.redirect-uri}")
    private String kakaoRedirectUri;
    
    public String getAuthorizationUrl(Provider provider) {
        switch (provider) {
            case GOOGLE:
                return "https://accounts.google.com/o/oauth2/v2/auth" +
                        "?client_id=" + googleClientId +
                        "&redirect_uri=" + URLEncoder.encode(googleRedirectUri, StandardCharsets.UTF_8) +
                        "&response_type=code" +
                        "&scope=email%20profile";
            case KAKAO:
                return "https://kauth.kakao.com/oauth/authorize" +
                        "?client_id=" + kakaoClientId +
                        "&redirect_uri=" + URLEncoder.encode(kakaoRedirectUri, StandardCharsets.UTF_8) +
                        "&response_type=code";
            default:
                throw new OAuth2AuthenticationException("Unsupported OAuth2 provider: " + provider);
        }
    }

    @Transactional
    public AuthResponse processOAuth2Callback(Provider provider, String code) {
        // 인증 코드로 액세스 토큰 교환
        OAuth2TokenResponse tokenResponse = oAuth2ClientService.getToken(provider, code);
        
        // 액세스 토큰으로 사용자 정보 조회
        OAuth2UserInfo userInfo = oAuth2ClientService.getUserInfo(provider, tokenResponse.getAccessToken());
        
        // 사용자 정보로 로그인 또는 회원가입 처리
        User user = processOAuth2User(userInfo, tokenResponse);
        
        // JWT 토큰 생성
        String accessToken = jwtTokenProvider.generateToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);
        
        AuthResponse authResponse = new AuthResponse();
        authResponse.setAccessToken(accessToken);
        authResponse.setRefreshToken(refreshToken);
        authResponse.setTokenType("Bearer");
      
        return authResponse;
    }
    
    // 추가: processOAuth2User 메서드 구현
    private User processOAuth2User(OAuth2UserInfo userInfo, OAuth2TokenResponse tokenResponse) {
        // 소셜 계정 검색
        Optional<SocialAccount> existingSocialAccount = 
                socialAccountRepository.findByProviderAndSocialId(userInfo.getProvider(), userInfo.getId());
        
        User user;
        if (existingSocialAccount.isPresent()) {
            // 기존 소셜 계정이 있으면 토큰 정보 업데이트
            SocialAccount socialAccount = existingSocialAccount.get();
            updateSocialAccountWithToken(socialAccount, userInfo, tokenResponse);
            user = socialAccount.getUser();
        } else {
            // 새로운 소셜 로그인인 경우
            Optional<User> existingUser = userRepository.findByEmail(userInfo.getEmail());
            
            if (existingUser.isPresent()) {
                // 이메일로 가입된 사용자가 있으면 소셜 계정 연결
                user = existingUser.get();
                createSocialAccountWithToken(user, userInfo, tokenResponse);
            } else {
                // 새 사용자 생성
                user = createUserFromOAuth2(userInfo);
                createSocialAccountWithToken(user, userInfo, tokenResponse);
            }
        }
        
        return user;
    }
    
    // 추가: 토큰 정보가 포함된 소셜 계정 생성
    private SocialAccount createSocialAccountWithToken(User user, OAuth2UserInfo userInfo, OAuth2TokenResponse tokenResponse) {
        SocialAccount socialAccount = SocialAccount.builder()
                .user(user)
                .provider(userInfo.getProvider())
                .socialId(userInfo.getId())
                .socialEmail(userInfo.getEmail())
                .socialName(userInfo.getName())
                .socialProfileImage(userInfo.getProfileImage())
                .accessToken(tokenResponse.getAccessToken())
                .refreshToken(tokenResponse.getRefreshToken())
                .tokenExpiresAt(LocalDateTime.now().plusSeconds(tokenResponse.getExpiresIn() != null ? tokenResponse.getExpiresIn() : 3600))
                .build();
        
        return socialAccountRepository.save(socialAccount);
    }
    
    // 추가: 토큰 정보가 포함된 소셜 계정 업데이트
    private void updateSocialAccountWithToken(SocialAccount socialAccount, OAuth2UserInfo userInfo, OAuth2TokenResponse tokenResponse) {
        socialAccount.setSocialEmail(userInfo.getEmail());
        socialAccount.setSocialName(userInfo.getName());
        socialAccount.setSocialProfileImage(userInfo.getProfileImage());
        socialAccount.setAccessToken(tokenResponse.getAccessToken());
        socialAccount.setRefreshToken(tokenResponse.getRefreshToken());
        socialAccount.setTokenExpiresAt(LocalDateTime.now().plusSeconds(tokenResponse.getExpiresIn() != null ? tokenResponse.getExpiresIn() : 3600));
        
        socialAccountRepository.save(socialAccount);
    }
    
    /**
     * 소셜 로그인 처리
     */
    @Transactional
    public AuthResponse processSocialLogin(SocialLoginRequest request) {
        // 소셜 로그인 제공자로부터 사용자 정보 가져오기
        OAuth2UserInfo userInfo = oAuth2ClientService.getUserInfo(request.getProvider(), request.getToken());
        
        // 소셜 계정 검색
        Optional<SocialAccount> existingSocialAccount = 
                socialAccountRepository.findByProviderAndSocialId(userInfo.getProvider(), userInfo.getId());
        
        User user;
        if (existingSocialAccount.isPresent()) {
            // 기존 소셜 계정이 있으면 토큰 정보 업데이트
            SocialAccount socialAccount = existingSocialAccount.get();
            updateSocialAccount(socialAccount, userInfo);
            user = socialAccount.getUser();
        } else {
            // 새로운 소셜 로그인인 경우
            Optional<User> existingUser = userRepository.findByEmail(userInfo.getEmail());
            
            if (existingUser.isPresent()) {
                // 이메일로 가입된 사용자가 있으면 소셜 계정 연결
                user = existingUser.get();
                createSocialAccount(user, userInfo);
            } else {
                // 새 사용자 생성
                user = createUserFromOAuth2(userInfo);
                createSocialAccount(user, userInfo);
            }
        }
        
        // JWT 토큰 생성 및 반환
        String accessToken = jwtTokenProvider.generateToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);
        
        AuthResponse authResponse = new AuthResponse();
        authResponse.setAccessToken(accessToken);
        authResponse.setRefreshToken(refreshToken);
        authResponse.setTokenType("Bearer");
        
        return authResponse;
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
    
    /**
     * SocialAccount 정보 업데이트
     */
    private void updateSocialAccount(SocialAccount socialAccount, OAuth2UserInfo userInfo) {
        socialAccount.setSocialEmail(userInfo.getEmail());
        socialAccount.setSocialName(userInfo.getName());
        socialAccount.setSocialProfileImage(userInfo.getProfileImage());
        socialAccount.setAccessToken(userInfo.getAccessToken());
        socialAccount.setRefreshToken(userInfo.getRefreshToken());
        socialAccount.setTokenExpiresAt(LocalDateTime.now().plusDays(60));
        
        socialAccountRepository.save(socialAccount);
    }
}
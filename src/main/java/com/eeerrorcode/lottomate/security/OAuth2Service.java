package com.eeerrorcode.lottomate.security;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.eeerrorcode.lottomate.domain.dto.user.AuthResponse;
import com.eeerrorcode.lottomate.domain.dto.user.OAuth2UserInfo;
import com.eeerrorcode.lottomate.domain.dto.user.SocialLoginRequest;
import com.eeerrorcode.lottomate.domain.entity.user.SocialAccount;
import com.eeerrorcode.lottomate.domain.entity.user.User;
import com.eeerrorcode.lottomate.domain.entity.user.User.Role;
import com.eeerrorcode.lottomate.repository.SocialAccountRepository;
import com.eeerrorcode.lottomate.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OAuth2Service {

    private final UserRepository userRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final OAuth2ClientService oAuth2ClientService;

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
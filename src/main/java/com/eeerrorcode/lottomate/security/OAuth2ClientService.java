package com.eeerrorcode.lottomate.security;

import com.eeerrorcode.lottomate.domain.dto.user.OAuth2TokenResponse;
import com.eeerrorcode.lottomate.domain.dto.user.OAuth2UserInfo;
import com.eeerrorcode.lottomate.domain.entity.user.SocialAccount.Provider;
import com.eeerrorcode.lottomate.security.exception.OAuth2AuthenticationException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Map;

@Service
// @RequiredArgsConstructor
public class OAuth2ClientService {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
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
    
    // 생성자에 @Qualifier 적용
    public OAuth2ClientService(RestTemplate restTemplate, 
                              @Qualifier("jacksonTemplateObjectMapper") ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }
    /**
     * 인증 코드로 액세스 토큰 교환
     */
    public OAuth2TokenResponse getToken(Provider provider, String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        
        switch (provider) {
            case GOOGLE:
                params.add("client_id", googleClientId);
                params.add("client_secret", googleClientSecret);
                params.add("code", code);
                params.add("redirect_uri", googleRedirectUri);
                params.add("grant_type", "authorization_code");
                
                return restTemplate.postForObject(
                    "https://oauth2.googleapis.com/token",
                    new HttpEntity<>(params, headers),
                    OAuth2TokenResponse.class
                );
                
            case KAKAO:
                params.add("client_id", kakaoClientId);
                params.add("client_secret", kakaoClientSecret);
                params.add("code", code);
                params.add("redirect_uri", kakaoRedirectUri);
                params.add("grant_type", "authorization_code");
                
                return restTemplate.postForObject(
                    "https://kauth.kakao.com/oauth/token",
                    new HttpEntity<>(params, headers),
                    OAuth2TokenResponse.class
                );
                
            default:
                throw new OAuth2AuthenticationException("Unsupported OAuth2 provider: " + provider);
        }
    }

    /**
     * 소셜 로그인 제공자에 따라 적절한 사용자 정보 가져오기
     */
    public OAuth2UserInfo getUserInfo(Provider provider, String token) {
        switch (provider) {
            case GOOGLE:
                return getGoogleUserInfo(token);
            case KAKAO:
                return getKakaoUserInfo(token);
            // 다른 소셜 로그인 제공자 추가 가능
            default:
                throw new OAuth2AuthenticationException("Unsupported OAuth2 provider: " + provider);
        }
    }

    /**
     * Google 사용자 정보 가져오기 (ID 토큰 디코딩 방식)
     */
    private OAuth2UserInfo getGoogleUserInfo(String idToken) {
        try {
            // ID 토큰의 페이로드 부분 추출 (JWT의 두 번째 부분)
            String[] parts = idToken.split("\\.");
            if (parts.length < 2) {
                throw new OAuth2AuthenticationException("Invalid ID token format");
            }
            
            // Base64 디코딩
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            
            // JSON 파싱
            Map<String, Object> claims = objectMapper.readValue(payload, Map.class);
            
            return OAuth2UserInfo.builder()
                    .id((String) claims.get("sub"))
                    .email((String) claims.get("email"))
                    .name((String) claims.get("name"))
                    .profileImage((String) claims.get("picture"))
                    .provider(Provider.GOOGLE)
                    .accessToken(idToken)
                    .build();
        } catch (Exception e) {
            // 디코딩 실패시 원래 API 호출 방식 시도
            try {
                return getGoogleUserInfoFromApi(idToken);
            } catch (Exception apiEx) {
                throw new OAuth2AuthenticationException("Failed to process Google token: " + e.getMessage() + " / API error: " + apiEx.getMessage());
            }
        }
    }

    /**
     * Google API를 호출하여 사용자 정보 가져오기 (액세스 토큰 방식)
     */
    private OAuth2UserInfo getGoogleUserInfoFromApi(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                "https://www.googleapis.com/oauth2/v3/userinfo",
                HttpMethod.GET,
                entity,
                Map.class
        );

        Map<String, Object> userAttributes = response.getBody();
        if (userAttributes == null) {
            throw new OAuth2AuthenticationException("Failed to get user info from Google API");
        }

        return OAuth2UserInfo.builder()
                .id((String) userAttributes.get("sub"))
                .email((String) userAttributes.get("email"))
                .name((String) userAttributes.get("name"))
                .profileImage((String) userAttributes.get("picture"))
                .provider(Provider.GOOGLE)
                .accessToken(accessToken)
                .build();
    }

    /**
     * Kakao 사용자 정보 가져오기
     */
    private OAuth2UserInfo getKakaoUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.GET,
                entity,
                Map.class
        );

        Map<String, Object> userAttributes = response.getBody();
        if (userAttributes == null) {
            throw new OAuth2AuthenticationException("Failed to get user info from Kakao");
        }

        Map<String, Object> kakaoAccount = (Map<String, Object>) userAttributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

        return OAuth2UserInfo.builder()
                .id(String.valueOf(userAttributes.get("id")))
                .email((String) kakaoAccount.get("email"))
                .name((String) profile.get("nickname"))
                .profileImage((String) profile.get("profile_image_url"))
                .provider(Provider.KAKAO)
                .accessToken(accessToken)
                .build();
    }
}
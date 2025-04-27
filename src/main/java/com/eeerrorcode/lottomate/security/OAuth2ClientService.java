package com.eeerrorcode.lottomate.security;

import com.eeerrorcode.lottomate.domain.dto.user.OAuth2TokenResponse;
import com.eeerrorcode.lottomate.domain.dto.user.OAuth2UserInfo;
import com.eeerrorcode.lottomate.domain.entity.user.SocialAccount.Provider;
import com.eeerrorcode.lottomate.security.exception.OAuth2AuthenticationException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.log4j.Log4j2;

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

/**
 * OAuth2 소셜 로그인 제공자와의 API 통신을 처리하는 서비스
 * 토큰 교환, 사용자 정보 조회 등의 실제 API 호출을 담당합니다.
 */
@Service
@Log4j2
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
     * 
     * @param provider 소셜 로그인 제공자
     * @param code 인증 코드
     * @return 토큰 응답 (액세스 토큰, 리프레시 토큰 등)
     * @throws OAuth2AuthenticationException 토큰 교환 실패 시
     */
    public OAuth2TokenResponse getToken(Provider provider, String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        
        try {
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
                    throw new OAuth2AuthenticationException("지원하지 않는 소셜 로그인 제공자: " + provider);
            }
        } catch (Exception e) {
            log.error("토큰 교환 중 오류 발생: provider={}, error={}", provider, e.getMessage(), e);
            throw new OAuth2AuthenticationException("토큰 교환 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 소셜 로그인 제공자에 따라 적절한 사용자 정보 가져오기
     * 
     * @param provider 소셜 로그인 제공자
     * @param token 액세스 토큰
     * @return 사용자 정보
     * @throws OAuth2AuthenticationException 사용자 정보 조회 실패 시
     */
    public OAuth2UserInfo getUserInfo(Provider provider, String token) {
        try {
            switch (provider) {
                case GOOGLE:
                    return getGoogleUserInfo(token);
                case KAKAO:
                    return getKakaoUserInfo(token);
                // 다른 소셜 로그인 제공자 추가 가능
                default:
                    throw new OAuth2AuthenticationException("지원하지 않는 소셜 로그인 제공자: " + provider);
            }
        } catch (Exception e) {
            log.error("사용자 정보 조회 중 오류 발생: provider={}, error={}", provider, e.getMessage(), e);
            throw new OAuth2AuthenticationException("사용자 정보 조회 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * Google 사용자 정보 가져오기 (ID 토큰 디코딩 방식)
     * 
     * @param idToken ID 토큰 또는 액세스 토큰
     * @return 사용자 정보
     * @throws Exception 디코딩 실패 시
     */
    private OAuth2UserInfo getGoogleUserInfo(String idToken) {
        try {
            // ID 토큰의 페이로드 부분 추출 (JWT의 두 번째 부분)
            String[] parts = idToken.split("\\.");
            if (parts.length < 2) {
                throw new OAuth2AuthenticationException("유효하지 않은 ID 토큰 형식");
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
            log.warn("ID 토큰 디코딩 실패, API 호출 방식으로 전환: {}", e.getMessage());
            try {
                return getGoogleUserInfoFromApi(idToken);
            } catch (Exception apiEx) {
                log.error("Google 사용자 정보 조회 실패: {}", apiEx.getMessage(), apiEx);
                throw new OAuth2AuthenticationException("Google 토큰 처리 실패: " + e.getMessage() + " / API 오류: " + apiEx.getMessage());
            }
        }
    }

    /**
     * Google API를 호출하여 사용자 정보 가져오기 (액세스 토큰 방식)
     * 
     * @param accessToken 액세스 토큰
     * @return 사용자 정보
     * @throws Exception API 호출 실패 시
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
            throw new OAuth2AuthenticationException("Google API에서 사용자 정보를 가져오지 못했습니다");
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
     * 
     * @param accessToken 액세스 토큰
     * @return 사용자 정보
     * @throws Exception API 호출 실패 시
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
            throw new OAuth2AuthenticationException("Kakao API에서 사용자 정보를 가져오지 못했습니다");
        }

        Long id = (Long) userAttributes.get("id");
        Map<String, Object> kakaoAccount = (Map<String, Object>) userAttributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

        String email = kakaoAccount.containsKey("email") ? (String) kakaoAccount.get("email") : null;
        String name = profile.containsKey("nickname") ? (String) profile.get("nickname") : null;
        String profileImage = profile.containsKey("profile_image_url") ? (String) profile.get("profile_image_url") : null;

        return OAuth2UserInfo.builder()
                .id(String.valueOf(id))
                .email(email)
                .name(name)
                .profileImage(profileImage)
                .provider(Provider.KAKAO)
                .accessToken(accessToken)
                .build();
    }
}
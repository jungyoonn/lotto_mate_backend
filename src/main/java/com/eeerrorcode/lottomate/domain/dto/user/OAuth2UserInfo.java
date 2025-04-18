package com.eeerrorcode.lottomate.domain.dto.user;

import com.eeerrorcode.lottomate.domain.entity.user.SocialAccount.Provider;

import lombok.Builder;
import lombok.Getter;

/**
 * 다양한 소셜 로그인 제공자로부터 표준화된 사용자 정보를 담는 DTO
 */
@Getter
@Builder
public class OAuth2UserInfo {
    private String id;          // 소셜 서비스의 고유 ID
    private String email;       // 소셜 계정 이메일
    private String name;        // 소셜 계정 이름
    private String profileImage;// 프로필 이미지 URL
    private Provider provider;  // 소셜 제공자 (GOOGLE, KAKAO 등)
    private String accessToken; // 소셜 로그인 액세스 토큰
    private String refreshToken;// 소셜 로그인 리프레시 토큰 (있을 경우)
}
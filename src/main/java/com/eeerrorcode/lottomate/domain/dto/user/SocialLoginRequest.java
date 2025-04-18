package com.eeerrorcode.lottomate.domain.dto.user;

import com.eeerrorcode.lottomate.domain.entity.user.SocialAccount.Provider;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SocialLoginRequest {
    private Provider provider; // "GOOGLE", "KAKAO", etc.
    private String token;    // 소셜 로그인 서비스에서 받은 액세스 토큰
}

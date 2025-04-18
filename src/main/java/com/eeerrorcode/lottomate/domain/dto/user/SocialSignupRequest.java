package com.eeerrorcode.lottomate.domain.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import com.eeerrorcode.lottomate.domain.entity.user.SocialAccount.Provider;

/**
 * 소셜 로그인 사용자가 추가 정보를 입력할 때 사용하는 DTO
 */
@Getter
@Setter
public class SocialSignupRequest {
    
    @Email
    @NotBlank
    private String email;
    
    @NotBlank
    private String name;
    
    private String phone;
    
    @NotBlank
    private Provider provider; // "GOOGLE", "KAKAO", etc.
    
    @NotBlank
    private String socialId; // 소셜 서비스의 고유 ID
}
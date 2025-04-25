package com.eeerrorcode.lottomate.domain.dto.user;

import com.eeerrorcode.lottomate.domain.entity.user.SocialAccount.Provider;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 통합 로그인 요청을 위한 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnifiedLoginRequest {
    
    @NotNull(message = "로그인 유형은 필수입니다")
    private LoginType loginType = LoginType.EMAIL_PASSWORD;

    // 일반 로그인용 필드
    private String email;
    private String password;
    
    // 소셜 로그인용 필드
    private Provider provider;
    private String socialToken;

    public enum LoginType {
      EMAIL_PASSWORD,  // 이메일/비밀번호 로그인
      SOCIAL           // 소셜 로그인
  }

}

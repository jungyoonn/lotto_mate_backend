package com.eeerrorcode.lottomate.domain.dto.user;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.*;

/**
 * 회원 정보 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponseDto {
    
    private Long id;
    private String email;
    private String name;
    private String phone;
    private String profileImage;
    private String role;
    private boolean emailVerified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 소셜 계정 정보 (필요시 추가)
    private boolean hasSocialAccounts;
    private boolean hasGoogleAccount;
    private boolean hasKakaoAccount;
}
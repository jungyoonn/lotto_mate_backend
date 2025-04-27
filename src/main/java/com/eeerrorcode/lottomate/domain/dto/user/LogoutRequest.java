package com.eeerrorcode.lottomate.domain.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 로그아웃 요청 DTO
 * 사용자의 로그아웃 처리를 위한 데이터를 담습니다.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogoutRequest {
    
    private String refreshToken;
    
    private boolean logoutAll = false;
}
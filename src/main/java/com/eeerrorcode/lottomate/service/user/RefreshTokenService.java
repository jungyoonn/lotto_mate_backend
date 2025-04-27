package com.eeerrorcode.lottomate.service.user;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eeerrorcode.lottomate.domain.dto.user.RefreshTokenDto;
import com.eeerrorcode.lottomate.domain.entity.user.RefreshToken;
import com.eeerrorcode.lottomate.domain.entity.user.User;
import com.eeerrorcode.lottomate.exeption.AuthenticationException;
import com.eeerrorcode.lottomate.exeption.ResourceNotFoundException;
import com.eeerrorcode.lottomate.repository.RefreshTokenRepository;
import com.eeerrorcode.lottomate.repository.UserRepository;
import com.eeerrorcode.lottomate.security.JwtTokenProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * 리프레시 토큰 관리를 위한 서비스
 * 리프레시 토큰의 생성, 검증, 갱신 등의 기능을 담당합니다.
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    
    @Value("${jwt.refresh-token-validity-in-seconds:2592000}")
    private long refreshTokenValidityInSeconds;
    
    /**
     * 새 리프레시 토큰을 생성하고 저장합니다.
     * 
     * @param userId 사용자 ID
     * @param token 리프레시 토큰 문자열
     * @param deviceInfo 기기 정보 (선택적)
     * @return 저장된 리프레시 토큰 DTO
     */
    @Transactional
    public RefreshTokenDto createRefreshToken(Long userId, String token, String deviceInfo) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다: " + userId));
        
        // 해당 사용자의 동일 기기 정보에 대한 이전 토큰이 있으면 대체
        Optional<RefreshToken> existingToken = refreshTokenRepository.findByUserIdAndDeviceInfo(userId, deviceInfo);
        if (existingToken.isPresent()) {
            RefreshToken refreshToken = existingToken.get();
            refreshToken.setToken(token);
            refreshToken.setExpiryDate(LocalDateTime.now().plusSeconds(refreshTokenValidityInSeconds));
            RefreshToken savedToken = refreshTokenRepository.save(refreshToken);
            return toDto(savedToken);
        }
        
        // 새 토큰 생성
        LocalDateTime expiryDate = LocalDateTime.now().plusSeconds(refreshTokenValidityInSeconds);
        
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(token)
                .expiryDate(expiryDate)
                .deviceInfo(deviceInfo)
                .build();
        
        RefreshToken savedToken = refreshTokenRepository.save(refreshToken);
        log.info("리프레시 토큰 생성: userId={}, deviceInfo={}, expiry={}", userId, deviceInfo, expiryDate);
        
        return toDto(savedToken);
    }
    
    /**
     * 리프레시 토큰을 검증하고 새 엑세스 토큰을 발급합니다.
     * 
     * @param refreshToken 검증할 리프레시 토큰
     * @return 새로 발급된 엑세스 토큰
     * @throws AuthenticationException 토큰이 유효하지 않거나 만료된 경우
     */
    @Transactional
    public String validateRefreshTokenAndGetAccessToken(String refreshToken) {
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new AuthenticationException("유효하지 않은 리프레시 토큰입니다."));
        
        if (!token.isValid()) {
            refreshTokenRepository.delete(token);
            throw new AuthenticationException("만료된 리프레시 토큰입니다. 다시 로그인해 주세요.");
        }
        
        // 토큰이 유효하면 새 액세스 토큰 발급
        User user = token.getUser();
        return jwtTokenProvider.generateToken(user.getEmail());
    }
    
    /**
     * 리프레시 토큰을 갱신합니다.
     * 
     * @param oldRefreshToken 기존 리프레시 토큰
     * @return 새로 발급된 리프레시 토큰과 액세스 토큰 정보
     * @throws AuthenticationException 토큰이 유효하지 않거나 만료된 경우
     */
    @Transactional
    public TokenRefreshResponseDto refreshToken(String oldRefreshToken) {
        RefreshToken token = refreshTokenRepository.findByToken(oldRefreshToken)
                .orElseThrow(() -> new AuthenticationException("유효하지 않은 리프레시 토큰입니다."));
        
        if (!token.isValid()) {
            refreshTokenRepository.delete(token);
            throw new AuthenticationException("만료된 리프레시 토큰입니다. 다시 로그인해 주세요.");
        }
        
        User user = token.getUser();
        
        // 새 액세스 토큰 생성
        String newAccessToken = jwtTokenProvider.generateToken(user.getEmail());
        
        // 새 리프레시 토큰 생성
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail());
        
        // 기존 리프레시 토큰 업데이트
        token.setToken(newRefreshToken);
        token.setExpiryDate(LocalDateTime.now().plusSeconds(refreshTokenValidityInSeconds));
        refreshTokenRepository.save(token);
        
        return TokenRefreshResponseDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .build();
    }
    
    /**
     * 로그아웃 처리: 리프레시 토큰 제거
     * 
     * @param token 제거할 리프레시 토큰
     */
    @Transactional
    public void logout(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(refreshToken -> {
            refreshTokenRepository.delete(refreshToken);
            log.info("리프레시 토큰 삭제 (로그아웃): userId={}", refreshToken.getUser().getId());
        });
    }
    
    /**
     * 특정 사용자의 모든 리프레시 토큰 제거 (모든 기기에서 로그아웃)
     * 
     * @param userId 사용자 ID
     */
    @Transactional
    public void logoutAll(Long userId) {
        List<RefreshToken> tokens = refreshTokenRepository.findByUserId(userId);
        if (!tokens.isEmpty()) {
            refreshTokenRepository.deleteByUserId(userId);
            log.info("사용자의 모든 리프레시 토큰 삭제: userId={}, count={}", userId, tokens.size());
        }
    }
    
    /**
     * 주기적으로 만료된 리프레시 토큰 정리 (매일 새벽 2시)
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        int deletedCount = refreshTokenRepository.deleteAllExpiredTokens(now);
        if (deletedCount > 0) {
            log.info("만료된 리프레시 토큰 정리 완료: {}개 삭제됨", deletedCount);
        }
    }
    // RefreshTokenService 클래스에 추가할 메서드입니다.

    /**
     * 사용자 ID로 모든 리프레시 토큰(세션) 조회
     * 
     * @param userId 사용자 ID
     * @return 리프레시 토큰 DTO 목록
     */
    @Transactional(readOnly = true)
    public List<RefreshTokenDto> getRefreshTokensByUserId(Long userId) {
        List<RefreshToken> tokens = refreshTokenRepository.findByUserId(userId);
        return tokens.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    /**
     * 리프레시 토큰 유효성 검증
     * 
     * @param refreshToken 검증할 리프레시 토큰
     * @return 유효 여부
     * @throws AuthenticationException 토큰이 존재하지 않을 경우
     */
    @Transactional(readOnly = true)
    public boolean validateRefreshToken(String refreshToken) {
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new AuthenticationException("유효하지 않은 리프레시 토큰입니다."));
        
        return token.isValid();
    }

    /**
     * RefreshToken 엔티티를 DTO로 변환
     */
    private RefreshTokenDto toDto(RefreshToken token) {
        return RefreshTokenDto.builder()
                .id(token.getId())
                .userId(token.getUser().getId())
                .token(token.getToken())
                .expiryDate(token.getExpiryDate())
                .deviceInfo(token.getDeviceInfo())
                .createdAt(token.getCreatedAt())
                .updatedAt(token.getUpdatedAt())
                .isValid(token.isValid())
                .build();
    }
    
    /**
     * 토큰 갱신 응답 DTO 내부 클래스
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TokenRefreshResponseDto {
        private String accessToken;
        private String refreshToken;
        private String tokenType;
    }
}
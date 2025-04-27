package com.eeerrorcode.lottomate.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.eeerrorcode.lottomate.domain.entity.user.RefreshToken;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    
    /**
     * 토큰 문자열로 리프레시 토큰을 조회합니다.
     * 
     * @param token 조회할 리프레시 토큰 문자열
     * @return 토큰 객체 (Optional)
     */
    Optional<RefreshToken> findByToken(String token);
    
    /**
     * 사용자 ID로 해당 사용자의 모든 리프레시 토큰을 조회합니다.
     * 
     * @param userId 사용자 ID
     * @return 사용자의 모든 리프레시 토큰 목록
     */
    List<RefreshToken> findByUserId(Long userId);
    
    /**
     * 특정 사용자의 특정 기기 정보에 해당하는 리프레시 토큰을 조회합니다.
     * 
     * @param userId 사용자 ID
     * @param deviceInfo 기기 정보
     * @return 해당하는 리프레시 토큰 (Optional)
     */
    Optional<RefreshToken> findByUserIdAndDeviceInfo(Long userId, String deviceInfo);
    
    /**
     * 토큰 문자열로 리프레시 토큰을 삭제합니다.
     * 
     * @param token 삭제할 리프레시 토큰 문자열
     */
    void deleteByToken(String token);
    
    /**
     * 사용자 ID로 해당 사용자의 모든 리프레시 토큰을 삭제합니다. (로그아웃 전체 처리용)
     * 
     * @param userId 사용자 ID
     */
    void deleteByUserId(Long userId);
    
    /**
     * 만료된 모든 토큰을 삭제합니다. (정기적인 청소 작업용)
     * 
     * @param now 현재 시간
     * @return 삭제된 토큰 수
     */
    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.expiryDate < ?1")
    int deleteAllExpiredTokens(LocalDateTime now);
}
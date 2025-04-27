
package com.eeerrorcode.lottomate.domain.entity.user;

import java.time.LocalDateTime;


import com.eeerrorcode.lottomate.domain.entity.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 리프레시 토큰을 저장하는 엔티티
 * 사용자의 로그인 세션 유지 및 JWT 액세스 토큰 갱신에 사용됩니다.
 */
@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 500)
    private String token;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;
    
    @Column(name = "device_info", length = 500)
    private String deviceInfo;  // 사용자 기기 정보 (선택적)
    
    /**
     * 리프레시 토큰이 현재 유효한지 확인합니다.
     * 
     * @return 현재 시간 기준 만료되지 않았으면 true, 만료되었으면 false
     */
    public boolean isValid() {
        return expiryDate.isAfter(LocalDateTime.now());
    }
}
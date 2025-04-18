package com.eeerrorcode.lottomate.domain.entity.user;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "social_accounts", 
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"user_id", "provider"}),
           @UniqueConstraint(columnNames = {"provider", "social_id"})
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class SocialAccount {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Provider provider; // 'GOOGLE', 'KAKAO', etc.
    
    @Column(name = "social_id", nullable = false)
    private String socialId; // 소셜 서비스의 고유 ID
    
    @Column(name = "social_email")
    private String socialEmail;
    
    @Column(name = "social_name")
    private String socialName;
    
    @Column(name = "social_profile_image")
    private String socialProfileImage;
    
    @Column(name = "access_token", length = 2000)  // 길이를 2000으로 늘림
    private String accessToken;

    @Column(name = "refresh_token", length = 2000)  // 리프레시 토큰도 함께 늘리기
    private String refreshToken;
    
    @Column(name = "token_expires_at")
    private LocalDateTime tokenExpiresAt;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum Provider {
        GOOGLE, KAKAO, NAVER, FACEBOOK
    }
}
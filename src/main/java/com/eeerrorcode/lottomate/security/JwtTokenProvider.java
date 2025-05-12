package com.eeerrorcode.lottomate.security;

import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.eeerrorcode.lottomate.domain.entity.user.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class JwtTokenProvider {

    @Value("${jwt.secret:c2VjdXJpdHkta2V5LWZvci1sb3R0by1hcHBsaWNhdGlvbi13aXRoLWp3dC10b2tlbi1tYW5hZ2VtZW50}")
    private String secretKey;
    
    @Value("${jwt.token-validity-in-seconds:86400}")
    private long tokenValidityInSeconds;
    
    @Value("${jwt.refresh-token-validity-in-seconds:2592000}")
    private long refreshTokenValidityInSeconds;

    private Key key;
    
    @PostConstruct
    public void init() {
        // 안전한 키 생성 (256비트 이상)
        this.key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    }

    // 액세스 토큰 생성
    public String generateToken(String email) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + tokenValidityInSeconds * 1000);

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key)
                .compact();
    }
    
    // 액세스 토큰 생성 (User 객체 파라미터)
    public String generateToken(User user) {
        return generateToken(user.getEmail());
    }
    
    // 리프레시 토큰 생성
    public String generateRefreshToken(String email) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshTokenValidityInSeconds * 1000);

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key)
                .compact();
    }
    
    // 리프레시 토큰 생성 (User 객체 파라미터)
    public String generateRefreshToken(User user) {
        return generateRefreshToken(user.getEmail());
    }
    
    public String getEmailFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * JWT 토큰에서 사용자 ID 추출
     * 
     * @param token JWT 토큰
     * @return 사용자 ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        // 토큰에서 userId 추출 (문자열이나 숫자로 저장되었을 수 있음)
        Object userId = claims.get("userId");
        if (userId instanceof Integer) {
            return ((Integer) userId).longValue();
        } else if (userId instanceof Long) {
            return (Long) userId;
        } else if (userId instanceof String) {
            try {
                return Long.parseLong((String) userId);
            } catch (NumberFormatException e) {
                log.error("토큰의 userId를 Long으로 변환할 수 없습니다: {}", userId);
                throw new JwtException("토큰의 userId 형식이 올바르지 않습니다.");
            }
        }
        
        log.error("토큰에서 userId를 찾을 수 없거나 알 수 없는 형식입니다: {}", userId);
        throw new JwtException("토큰에서 userId를 찾을 수 없습니다.");
    }

}
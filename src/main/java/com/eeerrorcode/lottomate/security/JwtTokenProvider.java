package com.eeerrorcode.lottomate.security;

import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.eeerrorcode.lottomate.domain.entity.user.User;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Component
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
}
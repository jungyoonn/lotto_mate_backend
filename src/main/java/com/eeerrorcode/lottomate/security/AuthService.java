package com.eeerrorcode.lottomate.security;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.eeerrorcode.lottomate.domain.dto.user.AuthResponse;
import com.eeerrorcode.lottomate.domain.dto.user.LoginRequest;
import com.eeerrorcode.lottomate.domain.dto.user.SignupRequest;
import com.eeerrorcode.lottomate.domain.entity.user.User;
import com.eeerrorcode.lottomate.domain.entity.user.User.Role;
import com.eeerrorcode.lottomate.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public void register(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .role(Role.USER)
                .isActive(true)
                .emailVerified(false)
                .build();

        userRepository.save(user);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));
    
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 틀렸습니다.");
        }
    
        // 액세스 토큰 생성
        String accessToken = jwtTokenProvider.generateToken(user.getEmail());
        
        // 리프레시 토큰 생성
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail());
        
        // 여기서 리프레시 토큰을 DB에 저장하는 코드를 추가할 수 있습니다.
        // 예: userRepository.updateRefreshToken(user.getId(), refreshToken);
    
        AuthResponse response = new AuthResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
    
        return response;
    }
}
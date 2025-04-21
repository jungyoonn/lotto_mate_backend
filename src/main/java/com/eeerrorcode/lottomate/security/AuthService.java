package com.eeerrorcode.lottomate.security;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.eeerrorcode.lottomate.domain.dto.user.AuthResponse;
import com.eeerrorcode.lottomate.domain.dto.user.LoginRequest;
import com.eeerrorcode.lottomate.domain.dto.user.SignupRequest;
import com.eeerrorcode.lottomate.domain.dto.user.UserRegistrationDto;
import com.eeerrorcode.lottomate.domain.entity.user.User;
import com.eeerrorcode.lottomate.exeption.RegistrationException;
import com.eeerrorcode.lottomate.repository.UserRepository;
import com.eeerrorcode.lottomate.service.user.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    public void register(SignupRequest request) {
        // 이메일 중복 확인
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RegistrationException("이미 사용 중인 이메일입니다: " + request.getEmail());
        }

        try {
            // SignupRequest를 UserRegistrationDto로 변환
            UserRegistrationDto registrationDto = UserRegistrationDto.builder()
                    .email(request.getEmail())
                    .password(request.getPassword())
                    .name(request.getName())
                    .build();
            
            // UserService를 통해 회원가입 처리
            userService.registerUser(registrationDto);
            
            log.info("회원가입 성공: {}", request.getEmail());
        } catch (Exception e) {
            log.error("회원가입 실패: {}", e.getMessage(), e);
            throw new RegistrationException("회원가입 처리 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
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
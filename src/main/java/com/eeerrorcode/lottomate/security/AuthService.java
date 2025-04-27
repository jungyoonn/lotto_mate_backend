package com.eeerrorcode.lottomate.security;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eeerrorcode.lottomate.domain.dto.user.AuthResponse;
import com.eeerrorcode.lottomate.domain.dto.user.LoginRequest;
import com.eeerrorcode.lottomate.domain.dto.user.RefreshTokenDto;
import com.eeerrorcode.lottomate.domain.dto.user.SignupRequest;
import com.eeerrorcode.lottomate.domain.dto.user.UserRegistrationDto;
import com.eeerrorcode.lottomate.domain.entity.user.User;
import com.eeerrorcode.lottomate.exeption.AuthenticationException;
import com.eeerrorcode.lottomate.exeption.RegistrationException;
import com.eeerrorcode.lottomate.repository.UserRepository;
import com.eeerrorcode.lottomate.service.user.RefreshTokenService;
import com.eeerrorcode.lottomate.service.user.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.Optional;

/**
 * 인증 관련 서비스
 * 회원가입, 로그인, 토큰 관리 등의 기능을 제공합니다.
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;

    /**
     * 회원가입 처리
     *
     * @param request 회원가입 요청 DTO
     * @throws RegistrationException 회원가입 실패 시
     */
    @Transactional
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

    /**
     * 로그인 처리
     *
     * @param request 로그인 요청 DTO
     * @return 인증 응답 (액세스 토큰, 리프레시 토큰)
     * @throws IllegalArgumentException 인증 실패 시
     */
    @Transactional
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
        
        // 리프레시 토큰 저장 (디바이스 정보는 클라이언트에서 전달할 수 있음)
        String deviceInfo = "WEB"; // 기본값, 실제로는 클라이언트에서 전달받을 수 있음
        refreshTokenService.createRefreshToken(user.getId(), refreshToken, deviceInfo);
    
        AuthResponse response = new AuthResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setTokenType("Bearer");
    
        return response;
    }

    /**
     * 통합 로그인 응답 처리
     *
     * @param user 인증된 사용자
     * @param deviceInfo 디바이스 정보
     * @return 인증 응답 (액세스 토큰, 리프레시 토큰)
     */
    @Transactional
    public AuthResponse processUnifiedLoginResponse(User user, String deviceInfo) {
        // 액세스 토큰 생성
        String accessToken = jwtTokenProvider.generateToken(user.getEmail());
        
        // 리프레시 토큰 생성
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail());
        
        // 리프레시 토큰 저장
        refreshTokenService.createRefreshToken(user.getId(), refreshToken, deviceInfo);
        
        AuthResponse response = new AuthResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setTokenType("Bearer");
        
        return response;
    }

    /**
     * 패스워드 검증
     *
     * @param rawPassword 검증할 원본 패스워드
     * @param encodedPassword 인코딩된 패스워드
     * @return 패스워드 일치 여부
     */
    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    /**
     * 사용자 ID로 활성 세션(리프레시 토큰) 목록 조회
     *
     * @param userId 사용자 ID
     * @return 리프레시 토큰 DTO 목록
     */
    @Transactional(readOnly = true)
    public List<RefreshTokenDto> getUserSessions(Long userId) {
        return refreshTokenService.getRefreshTokensByUserId(userId);
    }

    /**
     * 이메일로 사용자 조회
     *
     * @param email 사용자 이메일
     * @return 사용자 (Optional)
     */
    @Transactional(readOnly = true)
    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * ID로 사용자 조회
     *
     * @param id 사용자 ID
     * @return 사용자 (Optional)
     */
    @Transactional(readOnly = true)
    public Optional<User> findUserById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * 액세스 토큰으로부터 사용자 ID 추출
     *
     * @param accessToken 액세스 토큰
     * @return 사용자 ID (Optional)
     */
    public Optional<Long> getUserIdFromToken(String accessToken) {
        try {
            if (jwtTokenProvider.validateToken(accessToken)) {
                String email = jwtTokenProvider.getEmailFromToken(accessToken);
                return userRepository.findByEmail(email)
                        .map(User::getId);
            }
            return Optional.empty();
        } catch (Exception e) {
            log.warn("토큰에서 사용자 정보 추출 실패: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * 리프레시 토큰의 유효성 검증
     *
     * @param refreshToken 리프레시 토큰
     * @return 유효 여부
     */
    public boolean validateRefreshToken(String refreshToken) {
        try {
            return refreshTokenService.validateRefreshToken(refreshToken);
        } catch (AuthenticationException e) {
            log.warn("리프레시 토큰 검증 실패: {}", e.getMessage());
            return false;
        }
    }
}
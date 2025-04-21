package com.eeerrorcode.lottomate.service.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eeerrorcode.lottomate.domain.dto.user.*;
import com.eeerrorcode.lottomate.domain.entity.user.User;
import com.eeerrorcode.lottomate.domain.entity.user.User.Role;
import com.eeerrorcode.lottomate.exeption.AuthenticationException;
import com.eeerrorcode.lottomate.exeption.ResourceNotFoundException;
import com.eeerrorcode.lottomate.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@Transactional
@RequiredArgsConstructor
@Log4j2
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponseDto registerUser(UserRegistrationDto registrationDto) {
        // 이메일 중복 확인
        if (isEmailDuplicated(registrationDto.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다: " + registrationDto.getEmail());
        }
        
        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(registrationDto.getPassword());
        
        // 회원 엔티티 생성
        User user = User.builder()
                .email(registrationDto.getEmail())
                .password(encodedPassword)
                .name(registrationDto.getName())
                .phone(registrationDto.getPhone())
                .role(Role.USER)
                .isActive(true)
                .emailVerified(false) // 이메일 인증은 기본적으로 false로 설정
                .build();
        
        // 회원 저장
        User savedUser = userRepository.save(user);
        log.info("회원 가입 완료: id={}, email={}", savedUser.getId(), savedUser.getEmail());
        
        // 응답 DTO 변환
        return toUserResponseDto(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("회원을 찾을 수 없습니다: " + userId));
        
        return toUserResponseDto(user);
    }

    @Override
    public UserResponseDto updateUserInfo(Long userId, UserUpdateDto updateDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("회원을 찾을 수 없습니다: " + userId));
        
        // 이름이 제공된 경우 업데이트
        if (updateDto.getName() != null && !updateDto.getName().isEmpty()) {
            user.setName(updateDto.getName());
        }
        
        // 전화번호가 제공된 경우 업데이트
        if (updateDto.getPhone() != null) {
            user.setPhone(updateDto.getPhone());
        }
        
        // 프로필 이미지가 제공된 경우 업데이트
        if (updateDto.getProfileImage() != null) {
            user.setProfileImage(updateDto.getProfileImage());
        }
        
        User updatedUser = userRepository.save(user);
        log.info("회원 정보 수정 완료: id={}", updatedUser.getId());
        
        return toUserResponseDto(updatedUser);
    }

    @Override
    public void changePassword(Long userId, PasswordChangeDto passwordChangeDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("회원을 찾을 수 없습니다: " + userId));
        
        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(passwordChangeDto.getCurrentPassword(), user.getPassword())) {
            throw new AuthenticationException("현재 비밀번호가 일치하지 않습니다");
        }
        
        // 새 비밀번호 암호화 및 저장
        String newEncodedPassword = passwordEncoder.encode(passwordChangeDto.getNewPassword());
        user.setPassword(newEncodedPassword);
        
        userRepository.save(user);
        log.info("비밀번호 변경 완료: id={}", user.getId());
    }

    @Override
    public void deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("회원을 찾을 수 없습니다: " + userId));
        
        // 계정 비활성화
        user.setActive(false);
        
        userRepository.save(user);
        log.info("회원 계정 비활성화 완료: id={}", user.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEmailDuplicated(String email) {
        return userRepository.existsByEmail(email);
    }
    
    /**
     * User 엔티티를 UserResponseDto로 변환
     * 
     * @param user 회원 엔티티
     * @return 회원 응답 DTO
     */
    private UserResponseDto toUserResponseDto(User user) {
        if (user == null) {
            return null;
        }
        
        return UserResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .phone(user.getPhone())
                .profileImage(user.getProfileImage())
                .role(user.getRole().name())
                .emailVerified(user.isEmailVerified())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
package com.eeerrorcode.lottomate.service.user;

import com.eeerrorcode.lottomate.domain.dto.user.*;

public interface UserService {
    /**
     * 회원 가입 처리
     * 
     * @param registrationDto 회원가입 정보
     * @return 생성된 회원 정보
     */
    UserResponseDto registerUser(UserRegistrationDto registrationDto);
    
    /**
     * 회원 정보 조회
     * 
     * @param userId 회원 ID
     * @return 회원 정보
     */
    UserResponseDto getUserInfo(Long userId);
    
    /**
     * 회원 정보 수정
     * 
     * @param userId 회원 ID
     * @param updateDto 수정할 회원 정보
     * @return 수정된 회원 정보
     */
    UserResponseDto updateUserInfo(Long userId, UserUpdateDto updateDto);
    
    /**
     * 비밀번호 변경
     * 
     * @param userId 회원 ID
     * @param passwordChangeDto 비밀번호 변경 정보
     */
    void changePassword(Long userId, PasswordChangeDto passwordChangeDto);
    
    /**
     * 회원 계정 비활성화 (탈퇴)
     * 
     * @param userId 회원 ID
     */
    void deactivateUser(Long userId);
    
    /**
     * 이메일 중복 확인
     * 
     * @param email 확인할 이메일
     * @return 중복 여부 (true: 중복, false: 사용 가능)
     */
    boolean isEmailDuplicated(String email);
}
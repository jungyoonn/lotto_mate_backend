package com.eeerrorcode.lottomate.domain.dto.user;

import jakarta.validation.constraints.Pattern;
import lombok.*;

/**
 * 회원 정보 수정 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateDto {
    
    private String name;
    
    @Pattern(regexp = "^(01[016789])-?\\d{3,4}-?\\d{4}$", message = "유효한 전화번호 형식이 아닙니다")
    private String phone;
    
    private String profileImage;
}
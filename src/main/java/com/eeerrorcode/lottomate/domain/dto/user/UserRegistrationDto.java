package com.eeerrorcode.lottomate.domain.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * 회원가입 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationDto {
    
    @NotBlank(message = "이메일은 필수 입력 항목입니다")
    @Email(message = "유효한 이메일 형식이 아닙니다")
    private String email;
    
    @NotBlank(message = "비밀번호는 필수 입력 항목입니다")
    @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{8,}$", 
            message = "비밀번호는 최소 하나의 영문자와 숫자를 포함해야 합니다")
    private String password;
    
    @NotBlank(message = "이름은 필수 입력 항목입니다")
    private String name;
    
    @Pattern(regexp = "^(01[016789])-?\\d{3,4}-?\\d{4}$", message = "유효한 전화번호 형식이 아닙니다")
    private String phone;
}
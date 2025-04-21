package com.eeerrorcode.lottomate.domain.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * 비밀번호 변경 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordChangeDto {
    
    @NotBlank(message = "현재 비밀번호는 필수 입력 항목입니다")
    private String currentPassword;
    
    @NotBlank(message = "새 비밀번호는 필수 입력 항목입니다")
    @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$", 
            message = "비밀번호는 영문자, 숫자, 특수문자를 포함해야 합니다")
    private String newPassword;
    
    @NotBlank(message = "새 비밀번호 확인은 필수 입력 항목입니다")
    private String confirmPassword;
}
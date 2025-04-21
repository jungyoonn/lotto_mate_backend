package com.eeerrorcode.lottomate.domain.dto.user;

import lombok.*;

/**
 * 이메일 중복 확인 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailDuplicationResponseDto {
    
    private String email;
    private boolean duplicated;
}
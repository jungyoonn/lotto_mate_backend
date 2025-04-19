package com.eeerrorcode.lottomate.domain.dto;

import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Schema(description = "API 오류 응답 상세 정보")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
  @Schema(description = "오류 코드", example = "VALIDATION_ERROR")
  private String code;
  
  @Schema(description = "오류 메시지", example = "입력값 검증에 실패했습니다")
  private String message;
  
  @Schema(description = "필드별 오류 정보", example = "{\"email\":\"이메일 형식이 올바르지 않습니다\"}")
  private Map<String, String> errors;
}

package com.eeerrorcode.lottomate.exeption;

import java.util.HashMap;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.eeerrorcode.lottomate.domain.dto.CommonResponse;
import com.eeerrorcode.lottomate.domain.dto.ErrorResponse;

import lombok.extern.log4j.Log4j2;

import org.springframework.http.HttpStatus;
import java.util.Map;


@RestControllerAdvice
@Log4j2
public class GlobalExceptionHandler {
  /**
   * 리소스를 찾을 수 없는 예외 처리
   */
  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<CommonResponse<Void>> handleResourceNotFoundException(ResourceNotFoundException ex) {
    log.error("리소스를 찾을 수 없음: {}", ex.getMessage());
    
    return ResponseEntity
      .status(HttpStatus.NOT_FOUND)
      .body(CommonResponse.error("NOT_FOUND", ex.getMessage()));
  }
    
  /**
   * 구독 관련 예외 처리
   */
  @ExceptionHandler(SubscriptionException.class)
  public ResponseEntity<CommonResponse<Void>> handleSubscriptionException(SubscriptionException ex) {
    log.error("구독 예외: {}", ex.getMessage());
    
    return ResponseEntity
      .status(HttpStatus.BAD_REQUEST)
      .body(CommonResponse.error("SUBSCRIPTION_ERROR", ex.getMessage()));
  }
    
  /**
   * 결제 관련 예외 처리
   */
  @ExceptionHandler(PaymentException.class)
  public ResponseEntity<CommonResponse<Void>> handlePaymentException(PaymentException ex) {
    log.error("결제 예외: {}", ex.getMessage());
    
    return ResponseEntity
      .status(HttpStatus.BAD_REQUEST)
      .body(CommonResponse.error("PAYMENT_ERROR", ex.getMessage()));
  }
    
  /**
   * 결제 검증 예외 처리
   */
  @ExceptionHandler(PaymentVerificationException.class)
  public ResponseEntity<CommonResponse<Void>> handlePaymentVerificationException(PaymentVerificationException ex) {
    log.error("결제 검증 예외: {}", ex.getMessage());
    
    return ResponseEntity
      .status(HttpStatus.BAD_REQUEST)
      .body(CommonResponse.error("PAYMENT_VERIFICATION_ERROR", ex.getMessage()));
  }
  
  /**
   * 인증 관련 예외 처리
   */
  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<CommonResponse<Void>> handleAuthenticationException(AuthenticationException ex) {
    log.error("인증 예외: {}", ex.getMessage());
    
    return ResponseEntity
      .status(HttpStatus.UNAUTHORIZED)
      .body(CommonResponse.error("UNAUTHORIZED", ex.getMessage()));
  }
    
  /**
   * 입력값 검증 예외 처리
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<CommonResponse<Void>> handleValidationExceptions(MethodArgumentNotValidException ex) {
    log.error("유효성 검증 예외: {}", ex.getMessage());
    
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getAllErrors().forEach((error) -> {
      String fieldName = ((FieldError) error).getField();
      String errorMessage = error.getDefaultMessage();
      errors.put(fieldName, errorMessage);
    });
      
    ErrorResponse errorResponse = ErrorResponse.builder()
      .code("VALIDATION_ERROR")
      .message("입력값 검증에 실패했습니다")
      .errors(errors)
      .build();
    
    return ResponseEntity
      .status(HttpStatus.BAD_REQUEST)
      .body(CommonResponse.error(errorResponse));
  }
  /**
   * 회원가입 관련 예외 처리
   */
  @ExceptionHandler(RegistrationException.class)
  public ResponseEntity<CommonResponse<Void>> handleRegistrationException(RegistrationException ex) {
      log.error("회원가입 예외: {}", ex.getMessage());
      
      return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(CommonResponse.error("REGISTRATION_ERROR", ex.getMessage()));
  }

    /**
   * 기타 모든 예외 처리
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<CommonResponse<Void>> handleAllExceptions(Exception ex) {
    log.error("예상치 못한 예외 발생: ", ex);
    
    return ResponseEntity
      .status(HttpStatus.INTERNAL_SERVER_ERROR)
      .body(CommonResponse.error("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다"));
  }

}

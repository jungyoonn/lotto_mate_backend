package com.eeerrorcode.lottomate.exeption;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.eeerrorcode.lottomate.domain.ApiResponse;

import lombok.extern.log4j.Log4j2;

/**
 * 입력값 검증 실패 예외 처리를 담당하는 핸들러
 */
@RestControllerAdvice
@Log4j2
public class ValidationExceptionHandler {

    /**
     * Bean Validation 실패 예외 처리
     * 
     * @param ex MethodArgumentNotValidException
     * @return 필드별 오류 메시지가 포함된 응답
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        log.info("입력값 검증 실패: {}", errors);
        
        ApiResponse response = new ApiResponse("error", "입력값 검증에 실패했습니다");
        response.setData(errors);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * 회원가입 예외 처리
     * 
     * @param ex RegistrationException
     * @return 오류 메시지가 포함된 응답
     */
    @ExceptionHandler(RegistrationException.class)
    public ResponseEntity<ApiResponse> handleRegistrationException(RegistrationException ex) {
        log.error("회원가입 예외: {}", ex.getMessage());
        
        ApiResponse response = new ApiResponse("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
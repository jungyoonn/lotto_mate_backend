package com.eeerrorcode.lottomate.exeption;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 회원가입 과정에서 발생하는 예외
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class RegistrationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RegistrationException(String message) {
        super(message);
    }

    public RegistrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
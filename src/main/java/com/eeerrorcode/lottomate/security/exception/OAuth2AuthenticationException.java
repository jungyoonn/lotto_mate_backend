package com.eeerrorcode.lottomate.security.exception;

import org.springframework.security.core.AuthenticationException;

public class OAuth2AuthenticationException extends AuthenticationException {
    public OAuth2AuthenticationException(String msg) {
        super(msg);
    }
    
    public OAuth2AuthenticationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
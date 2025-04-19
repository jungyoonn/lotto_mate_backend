package com.eeerrorcode.lottomate.exeption;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class SubscriptionException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public SubscriptionException(String message) {
    super(message);
  }

  public SubscriptionException(String message, Throwable cause) {
    super(message, cause);
  }
}

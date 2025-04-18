package com.eeerrorcode.lottomate.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiResponse {
    private String state;
    private String message;
    private Object data;

    public ApiResponse(String state, String message) {
        this.state = state;
        this.message = message;
    }

    public ApiResponse(String state, String message, Object data) {
        this.state = state;
        this.message = message;
        this.data = data;
    }
}
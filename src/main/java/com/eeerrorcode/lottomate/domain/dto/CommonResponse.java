package com.eeerrorcode.lottomate.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CommonResponse<T> {
    private String message;
    private T data;
}

// ResponseBody<ApiResponse<>(담을 메세지, jsonBody 데이터)> 로 사용 
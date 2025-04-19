package com.eeerrorcode.lottomate.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "공용 API 응답 / 메세지 형식. ResponseBody<ApiResponse<>(담을 메세지, jsonBody 데이터)> 로 사용 ")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CommonResponse<T> {
    @Schema(description = "직접 지정한 응답 메세지 출력", example = "요청이 성공적으로 처리되었습니다")
    private String message;

    @Schema(description = "원래 추가되는 응답 데이터")
    private T data;

    @Schema(description = "에러 코드 또는 예외 설명", example = "NOT_FOUND / INVALID_PARAM / INTERNAL_ERROR")
    private String error;
}
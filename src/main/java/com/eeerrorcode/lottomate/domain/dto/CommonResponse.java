package com.eeerrorcode.lottomate.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "공용 API 응답 / 메세지 형식. ResponseBody<CommonResponse<>(담을 메세지, jsonBody 데이터)> 로 사용 ")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CommonResponse<T> {
  /**
   * 직접 지정한 응답 메세지 출력
   * 예시: "요청이 성공적으로 처리되었습니다"
   */
  @Schema(description = "직접 지정한 응답 메세지 출력", example = "요청이 성공적으로 처리되었습니다")
  private String message;
  /**
   * 원래 추가되는 응답 데이터
   */
  @Schema(description = "원래 추가되는 응답 데이터")
  private T data;
  /**
   * 에러 코드 또는 예외 설명
   * 예시: "NOT_FOUND / INVALID_PARAM / INTERNAL_ERROR"
   */
  @Schema(description = "에러 코드 또는 예외 설명", example = "NOT_FOUND / INVALID_PARAM / INTERNAL_ERROR")
  private String error;

    /**
     * 오류 응답 생성
     * @param errorCode 오류 코드
     * @param message 오류 메시지
     * @return API 응답 객체
     */
    public static <T> CommonResponse<T> error(String errorCode, String message) {
        return new CommonResponse<>(message, null, errorCode);
    }
    
    /**
     * 오류 응답 생성 (ErrorResponse 객체 사용)
     * @param errorResponse 오류 응답 객체
     * @return API 응답 객체
     */
    public static <T> CommonResponse<T> error(ErrorResponse errorResponse) {
        return new CommonResponse<>(errorResponse.getMessage(), null, errorResponse.getCode());
    }
    
    /**
     * 성공 응답 생성
     * @param data 응답 데이터
     * @param message 응답 메시지
     * @return API 응답 객체
     */
    public static <T> CommonResponse<T> success(T data, String message) {
        return new CommonResponse<>(message, data, null);
    }
    
    /**
     * 성공 응답 생성 (메시지 없음)
     * @param data 응답 데이터
     * @return API 응답 객체
     */
    public static <T> CommonResponse<T> success(T data) {
        return new CommonResponse<>("요청이 성공적으로 처리되었습니다", data, null);
    }
}
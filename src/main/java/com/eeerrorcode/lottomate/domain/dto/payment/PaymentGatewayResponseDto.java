package com.eeerrorcode.lottomate.domain.dto.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.*;

/**
 * 결제 게이트웨이(포트원) 응답 정보를 담는 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentGatewayResponseDto {
  private String impUid;          // 포트원 결제 고유번호
  private String merchantUid;      // 주문번호
  private BigDecimal amount;       // 결제/환불 금액
  private String status;           // 결제 상태 (paid, cancelled, ready 등)
  private String payMethod;        // 결제 수단
  private String receiptUrl;       // 영수증 URL
  
  // 카드 결제 정보
  private String cardName;         // 카드사 이름
  private String cardNumber;       // 마스킹된 카드번호
  
  // 가상계좌 정보
  private String vbankName;        // 가상계좌 은행명
  private String vbankNum;         // 가상계좌 번호
  private String vbankHolder;      // 가상계좌 예금주
  private LocalDateTime vbankDate; // 가상계좌 입금기한
  
  // 정기결제 정보
  private String customerUid;      // 구매자 식별키 (빌링키)
  
  // 에러 정보
  private String errorCode;        // 에러 코드
  private String errorMsg;         // 에러 메시지
  
  /**
   * 결제가 성공했는지 확인
   * @return 결제 성공 여부
   */
  public boolean isSuccessful() {
    return "paid".equals(status) || "ready".equals(status);
  }
  
  /**
   * 결제가 취소(환불)되었는지 확인
   * @return 결제 취소 여부
   */
  public boolean isCancelled() {
    return "cancelled".equals(status);
  }
  
  /**
   * 결제가 가상계좌 발급 상태인지 확인
   * @return 가상계좌 발급 여부
   */
  public boolean isVbankReady() {
    return "ready".equals(status) && "vbank".equals(payMethod);
  }
  
  /**
   * 오류가 있는지 확인
   * @return 오류 존재 여부
   */
  public boolean hasError() {
    return errorCode != null || errorMsg != null;
  }
}

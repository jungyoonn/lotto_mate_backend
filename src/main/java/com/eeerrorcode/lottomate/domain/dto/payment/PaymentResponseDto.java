package com.eeerrorcode.lottomate.domain.dto.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.eeerrorcode.lottomate.domain.entity.payment.PaymentMethodType;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponseDto {
  private Long id;
  private BigDecimal amount;
  private String paymentMethod;
  private String paymentStatus;
  private String merchantUid;
  private String impUid;
  private LocalDateTime paymentDate;
  private String receiptUrl;
  
  // 카드 결제인 경우 추가 정보
  private String cardName;
  private String cardNumber;
  
  // 환불된 경우 추가 정보
  private BigDecimal refundAmount;
  private LocalDateTime refundDate;
  
  // PG사 정보
  private String pgProvider;
  
  // 구독 관련 정보
  private Long subscriptionId;

  /**
   * 카드 정보를 담는 내부 클래스 (프론트엔드 표시용)
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CardInfo {
    private String issuer;    // 카드사명
    private String number;    // 마스킹된 카드번호
  }

  /**
   * 결제 수단 정보를 반환
   * @return 결제 수단 문자열 (예: "신용/체크카드", "카카오페이")
   */
  public String getFormattedPaymentMethod() {
    try {
      // 문자열을 열거형으로 변환 후 표시명 반환
      PaymentMethodType type = PaymentMethodType.valueOf(this.paymentMethod);
      return type.getDisplayName();
    } catch (IllegalArgumentException e) {
      // 열거형에 없는 값이면 원본 반환
      return this.paymentMethod;
    }
  }

  /**
   * 카드 정보 객체 생성
   * @return 카드 정보 객체
   */
  public CardInfo getCardInfo() {
    if (this.cardName != null && this.cardNumber != null) {
      return CardInfo.builder()
        .issuer(this.cardName)
        .number(this.cardNumber)
        .build();
    }
    return null;
  }
  
  /**
   * 환불 여부 확인
   * @return 환불 여부
   */
  public boolean isRefunded() {
    return this.refundAmount != null && this.refundAmount.compareTo(BigDecimal.ZERO) > 0;
  }
}

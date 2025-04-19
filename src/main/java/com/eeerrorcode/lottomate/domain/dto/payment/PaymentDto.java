package com.eeerrorcode.lottomate.domain.dto.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.eeerrorcode.lottomate.domain.entity.payment.PaymentStatus;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDto {
  private Long id;
  private Long userId;
  private Long subscriptionId;
  private BigDecimal amount;
  private String paymentMethod;
  private PaymentStatus paymentStatus;
  private String merchantUid; // IAMPORT 주문번호
  private String impUid; // IAMPORT 결제번호
  private LocalDateTime paymentDate;
  private BigDecimal refundAmount;
  private LocalDateTime refundDate;
  private String pgProvider; // PG사 정보
  private String cardName; // 카드사 정보
  private String cardNumber; // 마스킹된 카드번호
  private String bankName; // 은행명
  private String accountNumber; // 마스킹된 계좌번호
  private String receiptUrl; // 영수증 URL
}

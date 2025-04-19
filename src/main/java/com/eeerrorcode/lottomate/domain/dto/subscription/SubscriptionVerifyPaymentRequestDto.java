package com.eeerrorcode.lottomate.domain.dto.subscription;

import java.math.BigDecimal;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionVerifyPaymentRequestDto {
  // 결제가 완료되면 검증과 구독 활성화를 위해 사용, 검증이 완료되면 구독이 활성화됨

  private String impUid;       // 포트원 결제 고유번호
  private String merchantUid;  // 주문번호
  private String plan;         // 구독 플랜 (basic, standard, premium)
  private String period;       // 구독 기간 (monthly, yearly)
  private BigDecimal amount;   // 결제 금액
}

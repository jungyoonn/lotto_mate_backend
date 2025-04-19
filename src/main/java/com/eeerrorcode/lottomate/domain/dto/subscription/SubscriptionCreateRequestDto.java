package com.eeerrorcode.lottomate.domain.dto.subscription;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionCreateRequestDto {
  // 사용자가 새로운 구독을 생성하기 위해 요청할 때 필요한 데이터 dto

  private String plan; // basic, standard, premium
  private String period; // monthly, yearly
  private Long paymentMethodId; // 결제 수단 ID
  private boolean autoRenewal; // 자동 갱신 여부
}

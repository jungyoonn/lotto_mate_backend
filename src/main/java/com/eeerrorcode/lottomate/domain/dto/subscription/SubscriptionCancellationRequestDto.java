package com.eeerrorcode.lottomate.domain.dto.subscription;

import com.eeerrorcode.lottomate.domain.entity.payment.CancellationType;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionCancellationRequestDto {
  // 구독 취소 요청 dto
  private Long subscriptionId;
  private String reason;
  private CancellationType cancellationType;
  private boolean requestRefund;
}

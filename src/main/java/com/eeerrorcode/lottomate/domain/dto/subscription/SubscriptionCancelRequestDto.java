package com.eeerrorcode.lottomate.domain.dto.subscription;

import com.eeerrorcode.lottomate.domain.entity.payment.CancellationType;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionCancelRequestDto {
  private Long subscriptionId;
  private String reason;
  private CancellationType cancellationType;
}

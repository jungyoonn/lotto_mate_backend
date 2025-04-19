package com.eeerrorcode.lottomate.domain.dto.subscription;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionCancelRequestDto {
  private Long subscriptionId;
  private String reason;
}

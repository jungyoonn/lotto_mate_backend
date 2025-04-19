package com.eeerrorcode.lottomate.domain.dto.subscription;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionDetailsResponseDto {
  private Long id;
  private String plan;
  private String period;
  private BigDecimal amount;
  private LocalDateTime startDate;
  private LocalDateTime nextPaymentDate;
  private String merchantUid;
}

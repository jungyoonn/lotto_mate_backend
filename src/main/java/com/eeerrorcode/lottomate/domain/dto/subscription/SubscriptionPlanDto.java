package com.eeerrorcode.lottomate.domain.dto.subscription;

import java.math.BigDecimal;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionPlanDto {
  private Long id;
  private String name;
  private String description;
  private BigDecimal price;
  private int durationMonths;
  private int maxLottoNumbers;
  private String features;
  private boolean active;
}

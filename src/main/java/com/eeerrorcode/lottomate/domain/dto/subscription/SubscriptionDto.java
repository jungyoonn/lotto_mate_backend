package com.eeerrorcode.lottomate.domain.dto.subscription;

import java.time.LocalDateTime;

import com.eeerrorcode.lottomate.domain.dto.payment.PaymentMethodDto;
import com.eeerrorcode.lottomate.domain.entity.payment.SubscriptionStatus;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionDto {
  private Long id;
  private Long userId;
  private Long planId;
  private String planName;
  private SubscriptionStatus status;
  private LocalDateTime startDate;
  private LocalDateTime endDate;
  private boolean autoRenewal;
  private LocalDateTime nextPaymentDate;
  private PaymentMethodDto paymentMethod;
}

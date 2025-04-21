package com.eeerrorcode.lottomate.domain.dto.payment;

import com.eeerrorcode.lottomate.domain.entity.payment.PaymentMethodType;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentMethodDto {
  private Long id;
  private Long userId;
  private PaymentMethodType methodType;
  private boolean isDefault;
  private String cardName;
  private String cardNumber;
  private String cardExpiry;
  private String billingKey;
  private boolean isActive;
}

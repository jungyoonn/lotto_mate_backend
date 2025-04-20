package com.eeerrorcode.lottomate.domain.dto.payment;

import com.eeerrorcode.lottomate.domain.entity.payment.PaymentMethodType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentMethodCreateRequestDto {
  // 결제 수단을 등록할 때 사용

  @NotNull(message = "결제 수단 유형은 필수입니다")
  private PaymentMethodType methodType;
  
  private boolean isDefault;
  
  @NotBlank(message = "카드사 이름은 필수입니다")
  private String cardName;
  
  @NotBlank(message = "카드 번호는 필수입니다")
  private String cardNumber;
  
  private String cardExpiry;
  
  private String billingKey;
}

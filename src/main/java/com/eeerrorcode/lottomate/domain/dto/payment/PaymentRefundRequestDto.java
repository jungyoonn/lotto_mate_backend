package com.eeerrorcode.lottomate.domain.dto.payment;

import java.math.BigDecimal;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRefundRequestDto {
  // 결제 환불을 요청할 때 사용

  @NotBlank(message = "포트원 결제 고유번호는 필수입니다")
  private String impUid;
  
  @NotBlank(message = "주문번호는 필수입니다")
  private String merchantUid;
  
  @NotNull(message = "환불 금액은 필수입니다")
  @DecimalMin(value = "0.01", message = "환불 금액은 0보다 커야 합니다")
  private BigDecimal refundAmount;
  
  private String reason;
  
  private Long subscriptionId;
  
  private boolean cancelSubscription;
}

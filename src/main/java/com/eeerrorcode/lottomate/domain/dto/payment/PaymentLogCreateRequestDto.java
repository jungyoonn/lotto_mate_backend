package com.eeerrorcode.lottomate.domain.dto.payment;

import com.eeerrorcode.lottomate.domain.entity.payment.PaymentLogAction;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentLogCreateRequestDto {
  @NotNull(message = "결제 로그 액션은 필수입니다")
  private PaymentLogAction action;
  
  private Long paymentId;
  private String requestData;
  private String responseData;
}

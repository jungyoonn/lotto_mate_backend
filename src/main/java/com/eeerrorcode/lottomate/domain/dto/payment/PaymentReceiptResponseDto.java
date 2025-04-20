package com.eeerrorcode.lottomate.domain.dto.payment;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentReceiptResponseDto {
  // 결제 영수증 정보를 응답으로 반환

  private String impUid;
  private String merchantUid;
  private String receiptUrl;
  private String cardName;
  private String cardNumber;
  private String pgProvider;
}

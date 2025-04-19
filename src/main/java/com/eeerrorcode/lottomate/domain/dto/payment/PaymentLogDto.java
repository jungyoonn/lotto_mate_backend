package com.eeerrorcode.lottomate.domain.dto.payment;

import java.time.LocalDateTime;

import com.eeerrorcode.lottomate.domain.entity.payment.PaymentLogAction;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentLogDto {
  private Long id;
  private Long userId;
  private Long paymentId;
  private PaymentLogAction action;
  private String actionDisplay; // 액션 표시명
  private String requestData;
  private String responseData;
  private String ipAddress;
  private LocalDateTime createdAt;
  
  /**
   * 액션 표시명 설정
   */
  public void setActionDisplayFromAction() {
    if (this.action != null) {
      this.actionDisplay = this.action.getDisplayName();
    }
  }
}

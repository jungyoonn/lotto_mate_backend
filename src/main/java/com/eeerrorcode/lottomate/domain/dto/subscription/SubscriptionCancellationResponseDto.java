package com.eeerrorcode.lottomate.domain.dto.subscription;

import com.eeerrorcode.lottomate.domain.entity.payment.CancellationType;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubscriptionCancellationResponseDto {
  // 구독 취소 응답 dto
  private Long id;
  private Long subscriptionId;
  private String cancellationDate; // 포맷팅된 문자열
  private String effectiveEndDate; // 포맷팅된 문자열
  private CancellationType cancellationType;
  private String cancellationTypeDisplay;
  private boolean refundProcessed;
  private boolean adminProcessed;
  private String reason;
  
  // 추가 정보
  private String planName;
  private String period; // monthly/yearly
  private String userName;
  private String userEmail;
  private Long userId;
  
  /**
   * 취소 유형 표시명 설정
   */
  public void setCancellationTypeDisplayFromType() {
    if (this.cancellationType != null) {
      this.cancellationTypeDisplay = this.cancellationType.getDisplayName();
    }
  }
}

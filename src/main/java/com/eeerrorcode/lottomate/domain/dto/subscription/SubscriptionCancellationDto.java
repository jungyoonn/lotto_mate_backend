package com.eeerrorcode.lottomate.domain.dto.subscription;

import java.time.LocalDateTime;

import com.eeerrorcode.lottomate.domain.entity.payment.CancellationType;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubscriptionCancellationDto {
  // 구독 취소 이력 dto
  private Long id;
  private Long subscriptionId;
  private Long userId;
  private LocalDateTime cancellationDate;
  private String reason;
  private LocalDateTime effectiveEndDate;
  private boolean refundProcessed;
  private boolean adminProcessed;
  private String adminNote;
  private CancellationType cancellationType;
  private String cancellationTypeDisplay; // 화면 표시용 취소 유형
  
  // 추가 정보 (UI 표시용)
  private String planName;
  private String userEmail;
  private String userName;
}

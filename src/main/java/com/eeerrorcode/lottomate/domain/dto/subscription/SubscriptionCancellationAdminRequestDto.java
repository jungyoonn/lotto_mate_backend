package com.eeerrorcode.lottomate.domain.dto.subscription;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionCancellationAdminRequestDto {
  // 구독 취소 관리자 처리 요청 dto
  private Long cancellationId;
  private boolean adminProcessed;
  private String adminNote;
  private boolean processRefund;
}

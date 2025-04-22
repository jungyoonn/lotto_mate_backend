package com.eeerrorcode.lottomate.domain.dto.subscription;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.eeerrorcode.lottomate.domain.dto.payment.PaymentMethodDto;
import com.eeerrorcode.lottomate.domain.dto.payment.PaymentResponseDto;
import com.eeerrorcode.lottomate.domain.entity.payment.SubscriptionStatus;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionResponseDto {
  // 마이페이지에서 보여 줄 구독 정보 데이터

  private Long id;
  private String plan;
  private String period;
  private SubscriptionStatus status;
  private LocalDateTime startDate;
  private LocalDateTime endDate;
  private LocalDateTime nextPaymentDate;
  private boolean autoRenewal;
  private BigDecimal price;
  private PaymentMethodDto paymentMethod;
  private PaymentResponseDto recentPayment;

  // 관리자용 추가 정보
  private String userName;
  private String userEmail;
  private Long userId;

  // 클라이언트에 표시될 카드 정보를 담는 내부 클래스
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CardInfo {
    private String issuer;    // 카드사명
    private String number;    // 마스킹된 카드번호
  }
}

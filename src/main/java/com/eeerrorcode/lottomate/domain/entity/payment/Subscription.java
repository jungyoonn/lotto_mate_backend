package com.eeerrorcode.lottomate.domain.entity.payment;

import java.time.LocalDateTime;

import com.eeerrorcode.lottomate.domain.entity.common.BaseEntity;
import com.eeerrorcode.lottomate.domain.entity.user.User;

import jakarta.persistence.*;
import lombok.*;

@Entity(name = "subscriptions")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
public class Subscription extends BaseEntity{
  // 구독 정보 테이블
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "plan_id", nullable = false)
  private SubscriptionPlan plan;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private SubscriptionStatus status;

  @Column(name = "start_date", nullable = false)
  private LocalDateTime startDate;

  @Column(name = "end_date", nullable = true)
  private LocalDateTime endDate;

  @Column(name = "auto_renewal", nullable = false, columnDefinition = "boolean default true")
  private boolean autoRenewal;

  @Column(name = "next_payment_date", nullable = true)
  private LocalDateTime nextPaymentDate;
  
  @Column(name = "billing_key", nullable = true)
  private String billingKey;
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "payment_method_id", nullable = true)
  private PaymentMethod paymentMethod;

  /**
   * 구독 상태 업데이트
   * @param status 새로운 상태
  */
  public void updateStatus(SubscriptionStatus status) {
    this.status = status;
  }
  
  /**
   * 다음 결제일 업데이트
   * @param nextPaymentDate 다음 결제일
   */
  public void updateNextPaymentDate(LocalDateTime nextPaymentDate) {
    this.nextPaymentDate = nextPaymentDate;
  }
  
  /**
   * 종료일 설정
   * @param endDate 종료일
   */
  public void setEndDate(LocalDateTime endDate) {
    this.endDate = endDate;
  }
  
  /**
   * 자동 갱신 설정 변경
   * @param autoRenewal 자동 갱신 여부
   */
  public void setAutoRenewal(boolean autoRenewal) {
    this.autoRenewal = autoRenewal;
  }
  
  /**
   * 결제 수단 변경
   * @param paymentMethod 새로운 결제 수단
   */
  public void changePaymentMethod(PaymentMethod paymentMethod) {
    this.paymentMethod = paymentMethod;
  }
  
  /**
   * 빌링키 업데이트
   * @param billingKey 새로운 빌링키
   */
  public void updateBillingKey(String billingKey) {
    this.billingKey = billingKey;
  }
  
  /**
   * 구독 취소
   * 상태를 CANCELLED로 변경하고 자동 갱신을 비활성화
   */
  public void cancel() {
    this.status = SubscriptionStatus.CANCELLED;
    this.autoRenewal = false;
  }
  
  /**
   * 구독 활성화
   * 상태를 ACTIVE로 변경
   */
  public void activate() {
    this.status = SubscriptionStatus.ACTIVE;
  }
  
  /**
   * 구독이 활성 상태인지 확인
   * @return 활성 상태 여부
   */
  public boolean isActive() {
    return this.status == SubscriptionStatus.ACTIVE;
  }
}

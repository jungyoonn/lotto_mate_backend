package com.eeerrorcode.lottomate.domain.entity.payment;

import com.eeerrorcode.lottomate.domain.entity.common.BaseEntity;
import com.eeerrorcode.lottomate.domain.entity.user.User;

import jakarta.persistence.*;
import lombok.*;

@Entity(name = "payment_methods")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
public class PaymentMethod extends BaseEntity {
  // 결제 수단 테이블
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(name = "method_type", nullable = false)
  private PaymentMethodType methodType;
  
  @Column(name = "is_default", nullable = false, columnDefinition = "boolean default false")
  private boolean isDefault;
  
  @Column(name = "card_name", nullable = true)
  private String cardName;
  
  @Column(name = "card_number", nullable = true)
  private String cardNumber;
  
  @Column(name = "card_expiry", nullable = true)
  private String cardExpiry;
  
  @Column(name = "billing_key", nullable = true)
  private String billingKey;
  
  @Column(name = "is_active", nullable = false, columnDefinition = "boolean default true")
  private boolean isActive;
  
  /**
   * 결제 수단을 기본으로 설정
   */
  public void setAsDefault() {
    this.isDefault = true;
  }
  
  /**
   * 결제 수단을 기본에서 해제
   */
  public void unsetDefault() {
    this.isDefault = false;
  }
  
  /**
   * 결제 수단 활성화
   */
  public void activate() {
    this.isActive = true;
  }
  
  /**
   * 결제 수단 비활성화
   */
  public void deactivate() {
    this.isActive = false;
  }
  
  /**
   * 카드 정보 설정
   * @param cardName 카드사 이름
   * @param cardNumber 마스킹된 카드 번호
   * @param cardExpiry 카드 만료일 (MMYY 형식)
   */
  public void setCardInfo(String cardName, String cardNumber, String cardExpiry) {
    this.cardName = cardName;
    this.cardNumber = cardNumber;
    this.cardExpiry = cardExpiry;
  }
  
  /**
   * 빌링키 설정
   * @param billingKey 정기결제용 키
   */
  public void setBillingKey(String billingKey) {
    this.billingKey = billingKey;
  }
  
  /**
   * 결제 수단이 카드인지 확인
   * @return 카드 여부
   */
  public boolean isCard() {
    return this.methodType == PaymentMethodType.CARD;
  }
  
  /**
   * 결제 수단이 기본으로 설정되었는지 확인
   * @return 기본 설정 여부
   */
  public boolean isDefault() {
    return this.isDefault;
  }
  
  /**
   * 결제 수단이 활성화되었는지 확인
   * @return 활성화 여부
   */
  public boolean isActive() {
    return this.isActive;
  }
  
  /**
   * 결제 수단에 빌링키가 있는지 확인
   * @return 빌링키 존재 여부
   */
  public boolean hasBillingKey() {
    return this.billingKey != null && !this.billingKey.isEmpty();
  }
}
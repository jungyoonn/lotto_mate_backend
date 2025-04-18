package com.eeerrorcode.lottomate.domain.entity.payment;

import com.eeerrorcode.lottomate.domain.entity.common.BaseEntity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity(name = "subscription_plans")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
public class SubscriptionPlan extends BaseEntity {
  //구독 플랜 테이블
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @Column(columnDefinition = "text", nullable = true)
  private String description;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal price;

  @Column(nullable = false, name = "duration_months")
  private int durationMonths;

  // 최대 저장 가능한 번호 갯수
  @Column(nullable = false, name = "max_lotto_numbers")
  private int maxLottoNumbers;

  // 추가 기능들(JSON 형태)
  @Column(columnDefinition = "text", nullable = true)
  private String features;

  // 활성 상태 여부
  @Column(nullable = false, name = "is_active", columnDefinition = "boolean default true")
  private boolean isActive;
  
  /**
   * 구독 플랜 활성화
   */
  public void activate() {
    this.isActive = true;
  }
  
  /**
   * 구독 플랜 비활성화
   */
  public void deactivate() {
    this.isActive = false;
  }
  
  /**
   * 플랜 정보 업데이트
   * @param name 플랜 이름
   * @param description 플랜 설명
   * @param price 가격
   * @param durationMonths 구독 기간(월)
   * @param maxLottoNumbers 최대 로또 번호 개수
   * @param features 추가 기능(JSON)
   */
  public void updatePlan(
    String name, 
    String description, 
    BigDecimal price,
    int durationMonths,
    int maxLottoNumbers,
    String features) {
    
    if (name != null) {
      this.name = name;
    }
    
    this.description = description;
    
    if (price != null) {
      this.price = price;
    }
    
    if (durationMonths > 0) {
      this.durationMonths = durationMonths;
    }
    
    if (maxLottoNumbers > 0) {
      this.maxLottoNumbers = maxLottoNumbers;
    }
    
    this.features = features;
  }
  
  /**
   * 구독 플랜 이름과 가격 정보 반환
   * @return 구독 플랜 요약 정보
   */
  public String getSummary() {
    return String.format("%s - %s원 / %d개월", 
      this.name, 
      this.price.toString(),
      this.durationMonths);
  }
  
  /**
   * 해당 플랜이 활성 상태인지 확인
   * @return 활성 상태 여부
   */
  public boolean isAvailable() {
    return this.isActive;
  }
}

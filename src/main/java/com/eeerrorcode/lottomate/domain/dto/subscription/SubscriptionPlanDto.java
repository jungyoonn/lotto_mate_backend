package com.eeerrorcode.lottomate.domain.dto.subscription;

import java.math.BigDecimal;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionPlanDto {
  private Long id;
  private String name;
  private String description;
  private BigDecimal price;
  private int durationMonths;
  private int maxLottoNumbers;
  private String features;
  private boolean active;

  /**
   * 구독 플랜 활성화
   */
  public void activate() {
    this.active = true;
  }
  
  /**
   * 구독 플랜 비활성화
   */
  public void deactivate() {
    this.active = false;
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
    return this.active;
  }
}

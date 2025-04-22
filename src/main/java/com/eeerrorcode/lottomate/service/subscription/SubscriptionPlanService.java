package com.eeerrorcode.lottomate.service.subscription;

import java.util.List;
import java.util.stream.Collectors;

import com.eeerrorcode.lottomate.domain.dto.subscription.SubscriptionPlanDto;
import com.eeerrorcode.lottomate.domain.entity.payment.SubscriptionPlan;


public interface SubscriptionPlanService {
  /**
   * 구독 플랜 이름으로 플랜 정보 조회
   * 
   * @param plan 플랜 이름 (basic, standard, premium)
   * @return 플랜 정보 Dto
   */
  SubscriptionPlanDto getPlanByName(String plan);
  
  /**
   * 모든 활성화된 구독 플랜 조회
   * 
   * @return 구독 플랜 목록
   */
  List<SubscriptionPlanDto> getAllActivePlans();
  
  /**
   * 모든 구독 플랜 조회 (활성/비활성 모두)
   * 
   * @return 구독 플랜 목록
   */
  List<SubscriptionPlanDto> getAllPlans();
  
  /**
   * 구독 플랜 등록
   * 
   * @param planDto 등록할 구독 플랜 정보
   * @return 등록된 구독 플랜 ID
   */
  Long createPlan(SubscriptionPlanDto planDto);
  
  /**
   * 구독 플랜 수정
   * 
   * @param planId 수정할 플랜 ID
   * @param planDto 수정할 플랜 정보
   * @return 수정된 구독 플랜 Dto
   */
  SubscriptionPlanDto updatePlan(Long planId, SubscriptionPlanDto planDto);
  
  /**
   * 구독 플랜 삭제 
   * 
   * @param planId 삭제할 플랜 ID
   * @return 삭제 성공 여부
   */
  boolean deletePlan(Long planId);
  
  /**
   * 구독 플랜 비활성 / 활성 토글
   * 
   * @param planId 활성/비활성할 플랜 ID
   * @return true : 활성. false : 비활성
   */
  boolean toggleActive(Long planId);
  
  /**
   * 구독 플랜 ID로 플랜 정보 조회
   * 
   * @param planId 플랜 ID
   * @return 플랜 정보 Dto
   */
  SubscriptionPlanDto getPlanById(Long planId);

  /**
   * SubscriptionPlan 엔티티를 SubscriptionPlanDto로 변환
   * 
   * @param plan 구독 플랜 엔티티
   * @return 구독 플랜 Dto
   */
  default SubscriptionPlanDto toDto(SubscriptionPlan plan) {
    if (plan == null) {
      return null;
    }
    
    return SubscriptionPlanDto.builder()
      .id(plan.getId())
      .name(plan.getName())
      .description(plan.getDescription())
      .price(plan.getPrice())
      .durationMonths(plan.getDurationMonths())
      .maxLottoNumbers(plan.getMaxLottoNumbers())
      .features(plan.getFeatures())
      .active(plan.isAvailable())
      .build();
  }
  
  /**
   * SubscriptionPlanDto를 SubscriptionPlan 엔티티로 변환
   * 
   * @param dto 구독 플랜 DTO
   * @return 구독 플랜 엔티티
   */
  default SubscriptionPlan toEntity(SubscriptionPlanDto dto) {
    if (dto == null) {
      return null;
    }
    
    SubscriptionPlan plan = SubscriptionPlan.builder()
      .id(dto.getId())
      .name(dto.getName())
      .description(dto.getDescription())
      .price(dto.getPrice())
      .durationMonths(dto.getDurationMonths())
      .maxLottoNumbers(dto.getMaxLottoNumbers())
      .features(dto.getFeatures())
      .build();
      
    // 활성 상태 설정
    if (dto.isActive()) {
      plan.activate();
    } else {
      plan.deactivate();
    }
    
    return plan;
  }
  
  /**
   * SubscriptionPlan 엔티티 리스트를 SubscriptionPlanDto 리스트로 변환
   * 
   * @param plans 구독 플랜 엔티티 리스트
   * @return 구독 플랜 DTO 리스트
   */
  default List<SubscriptionPlanDto> toDtoList(List<SubscriptionPlan> plans) {
    if (plans == null) {
      return null;
    }
    
    return plans.stream()
      .map(this::toDto)
      .collect(Collectors.toList());
  }
}

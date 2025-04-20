package com.eeerrorcode.lottomate.service.subscription;

import java.util.List;

import org.springframework.stereotype.Service;

import com.eeerrorcode.lottomate.domain.dto.subscription.SubscriptionPlanDto;
import com.eeerrorcode.lottomate.exeption.ResourceNotFoundException;
import com.eeerrorcode.lottomate.repository.payment.SubscriptionPlanRepository;
import com.eeerrorcode.lottomate.repository.payment.SubscriptionRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@Transactional
@Log4j2
@RequiredArgsConstructor
public class SubscriptionPlanServiceImpl implements SubscriptionPlanService{
  private final SubscriptionPlanRepository subscriptionPlanRepository;
  private final SubscriptionRepository subscriptionRepository;

  @Override
  public SubscriptionPlanDto getPlanByName(String plan) {
    // 구독 플랜 이름(basic, standard, premium)으로 구독 플랜 조회
    return subscriptionPlanRepository.findByNameIgnoreCase(plan)
      .map(this::toDto)
      .orElse(null);
  }

  @Override
  public List<SubscriptionPlanDto> getAllActivePlans() {
    // 활성화된 모든 구독 플랜 조회
    return toDtoList(subscriptionPlanRepository.findByIsActiveTrue());
  }

  @Override
  public List<SubscriptionPlanDto> getAllPlans() {
    // 모든 구독 플랜 조회 (활성/비활성 모두)
    return toDtoList(subscriptionPlanRepository.findAll());
  }

  @Override
  public Long createPlan(SubscriptionPlanDto planDto) {
    // 동일한 이름의 플랜이 이미 존재하는지 확인
    if (subscriptionPlanRepository.findByNameIgnoreCase(planDto.getName()).isPresent()) {
      log.warn("동일한 이름의 구독 플랜이 이미 존재합니다: {}", planDto.getName());
      throw new IllegalArgumentException("동일한 이름의 구독 플랜이 이미 존재합니다: " + planDto.getName());
    }
    
    Long planId = subscriptionPlanRepository.save(toEntity(planDto)).getId();
    log.info("구독 플랜 등록 완료: id = {}, name = {}", planId, planDto.getName());
    
    return planId;
  }

  @Override
  public SubscriptionPlanDto updatePlan(Long planId, SubscriptionPlanDto planDto) {
    // 수정할 플랜 조회
    SubscriptionPlanDto existingPlanDto = toDto(subscriptionPlanRepository.findById(planId)
      .orElseThrow(() -> new ResourceNotFoundException("구독 플랜을 찾을 수 없습니다: " + planId)));
    
    // 이름 변경 시 중복 체크
    if (!existingPlanDto.getName().equalsIgnoreCase(planDto.getName()) &&
        subscriptionPlanRepository.findByNameIgnoreCase(planDto.getName()).isPresent()) {
      log.warn("변경하려는 이름의 구독 플랜이 이미 존재합니다: {}", planDto.getName());
      throw new IllegalArgumentException("변경하려는 이름의 구독 플랜이 이미 존재합니다: " + planDto.getName());
    }
    
    // 플랜 정보 업데이트
    existingPlanDto.updatePlan(
      planDto.getName(),
      planDto.getDescription(),
      planDto.getPrice(),
      planDto.getDurationMonths(),
      planDto.getMaxLottoNumbers(),
      planDto.getFeatures()
    );
    
    // 활성 상태 설정
    if (planDto.isActive()) {
      existingPlanDto.activate();
    } else {
      existingPlanDto.deactivate();
    }
    
    // 변경사항 저장
    SubscriptionPlanDto updatedPlanDto = toDto(subscriptionPlanRepository.save(toEntity(existingPlanDto)));
    log.info("구독 플랜 수정 완료: id = {}, name = {}", updatedPlanDto.getId(), updatedPlanDto.getName());
    
    return updatedPlanDto;
  }

  @Override
  public boolean deletePlan(Long planId) {
    // 삭제(비활성화)할 플랜 조회
    SubscriptionPlanDto planDto = toDto(subscriptionPlanRepository.findById(planId)
      .orElseThrow(() -> new ResourceNotFoundException("구독 플랜을 찾을 수 없습니다: " + planId)));
    
    // 현재 활성 상태인 구독에서 사용 중인지 확인
    long activeSubscriptionsCount = subscriptionRepository.countByPlanIdAndStatus(
      planId, com.eeerrorcode.lottomate.domain.entity.payment.SubscriptionStatus.ACTIVE);
    
    if (activeSubscriptionsCount > 0) {
      log.warn("현재 사용 중인 구독 플랜은 삭제할 수 없습니다: id = {}, activeCount = {}", 
        planId, activeSubscriptionsCount);
      return false;
    }
    
    // 플랜 비활성화
    planDto.deactivate();
    Long deactivatePlanId = subscriptionPlanRepository.save(toEntity(planDto)).getId();
    log.info("구독 플랜 비활성화 완료: id = {}, name = {}", deactivatePlanId, planDto.getName());
    
    return true;
  }

  @Override
  public SubscriptionPlanDto getPlanById(Long planId) {
    return subscriptionPlanRepository.findById(planId)
      .map(this::toDto)
      .orElse(null);
  }
}

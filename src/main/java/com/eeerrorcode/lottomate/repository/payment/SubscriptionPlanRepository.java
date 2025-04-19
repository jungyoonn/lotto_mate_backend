package com.eeerrorcode.lottomate.repository.payment;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.eeerrorcode.lottomate.domain.entity.payment.SubscriptionPlan;

@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long>{
  /**
   * 활성 상태인 모든 구독 플랜 조회
   */
  List<SubscriptionPlan> findByIsActiveTrue();
  
  /**
   * 플랜 이름으로 구독 플랜 조회 (대소문자 무시)
   */
  Optional<SubscriptionPlan> findByNameIgnoreCase(String name);
}

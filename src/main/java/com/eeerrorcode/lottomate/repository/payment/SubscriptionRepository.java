package com.eeerrorcode.lottomate.repository.payment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.eeerrorcode.lottomate.domain.entity.payment.Subscription;
import com.eeerrorcode.lottomate.domain.entity.payment.SubscriptionStatus;


@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long>{
  /**
   * 사용자 ID와 상태로 구독 조회 (생성일 기준 내림차순)
   */
  List<Subscription> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, SubscriptionStatus status);
  
  /**
   * 사용자 ID로 모든 구독 조회 (생성일 기준 내림차순)
   */
  List<Subscription> findByUserIdOrderByCreatedAtDesc(Long userId);
  
  /**
   * 사용자 ID와 구독 ID로 구독 조회
   */
  Optional<Subscription> findByIdAndUserId(Long id, Long userId);
  
  /**
   * 다음 결제일이 지정된 날짜 이전인 활성 구독 조회
   */
  List<Subscription> findByStatusAndNextPaymentDateBeforeAndAutoRenewalTrue(
    SubscriptionStatus status, LocalDateTime date);
  
  /**
   * 결제 수단 ID와 상태로 구독 존재 여부 확인
   */
  boolean existsByPaymentMethodIdAndStatusIn(Long paymentMethodId, List<SubscriptionStatus> statuses);
  
  /**
   * 플랜 ID로 활성 구독 수 조회
   */
  long countByPlanIdAndStatus(Long planId, SubscriptionStatus status);
}

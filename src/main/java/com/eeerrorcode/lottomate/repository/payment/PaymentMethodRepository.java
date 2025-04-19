package com.eeerrorcode.lottomate.repository.payment;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.eeerrorcode.lottomate.domain.entity.payment.PaymentMethod;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long>{
  /**
   * 사용자 ID로 활성 결제 수단 목록 조회
   */
  List<PaymentMethod> findByUserIdAndIsActiveTrue(Long userId);
  
  /**
   * 사용자 ID와 기본 설정 여부로 결제 수단 조회
   */
  Optional<PaymentMethod> findByUserIdAndIsDefaultTrue(Long userId);
  
  /**
   * 결제 수단 ID와 사용자 ID로 결제 수단 조회
   */
  Optional<PaymentMethod> findByIdAndUserId(Long id, Long userId);
  
  /**
   * 사용자 ID로 활성 상태이고 특정 ID가 아닌 첫 번째 결제 수단 조회
   */
  Optional<PaymentMethod> findFirstByUserIdAndIsActiveTrueAndIdNot(Long userId, Long excludeId);
}

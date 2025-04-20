package com.eeerrorcode.lottomate.repository.payment;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.eeerrorcode.lottomate.domain.entity.payment.PaymentLog;
import com.eeerrorcode.lottomate.domain.entity.payment.PaymentLogAction;

@Repository
public interface PaymentLogRepository extends JpaRepository<PaymentLog, Long>{
  /**
   * 사용자 ID로 결제 로그 조회 (생성일 기준 내림차순)
   */
  List<PaymentLog> findByUserIdOrderByCreatedAtDesc(Long userId);
  
  /**
   * 사용자 ID로 결제 로그 페이징 조회 (생성일 기준 내림차순)
   */
  Page<PaymentLog> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
  
  /**
   * 사용자 ID와 액션으로 결제 로그 조회 (생성일 기준 내림차순)
   */
  List<PaymentLog> findByUserIdAndActionOrderByCreatedAtDesc(Long userId, PaymentLogAction action);
  
  /**
   * 결제 ID로 결제 로그 조회 (생성일 기준 내림차순)
   */
  List<PaymentLog> findByPaymentIdOrderByCreatedAtDesc(Long paymentId);
}

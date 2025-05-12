package com.eeerrorcode.lottomate.repository.payment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.eeerrorcode.lottomate.domain.entity.payment.Payment;
import com.eeerrorcode.lottomate.domain.entity.payment.PaymentStatus;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
  /**
   * 사용자 ID로 결제 목록 조회 (결제일 기준 내림차순)
   */
  Page<Payment> findByUserIdOrderByPaymentDateDesc(Long userId, Pageable pageable);
  
  /**
   * 구독 ID와 결제 상태로 가장 최근 결제 조회
   */
  Optional<Payment> findTopBySubscriptionIdAndPaymentStatusOrderByPaymentDateDesc(
    Long subscriptionId, PaymentStatus paymentStatus);
  
  /**
   * 포트원 결제 고유번호와 사용자 ID로 결제 조회
   */
  Optional<Payment> findByImpUidAndUserId(String impUid, Long userId);

  /**
   * 포트원 결제 고유번호로 결제 조회 (사용자 ID 무관)
   */
  Optional<Payment> findByImpUid(String impUid);
  
  /**
   * 사용자 ID와 결제 ID로 결제 조회
   */
  Optional<Payment> findByIdAndUserId(Long id, Long userId);
  
  /**
   * 결제 상태와 결제일 기간으로 결제 목록 조회
   */
  List<Payment> findByPaymentStatusAndPaymentDateBetween(
    PaymentStatus paymentStatus, LocalDateTime startDate, LocalDateTime endDate);
  
  /**
   * 특정 기간 동안의 총 결제 금액 합계 조회를 위한 메서드
   */
  /*
  @Query(value = "SELECT SUM(amount) FROM payments " +
                "WHERE payment_status = :status " +
                "AND payment_date BETWEEN :startDate AND :endDate", 
          nativeQuery = true)
  BigDecimal sumPaymentAmountByStatusAndDateBetween(
      @Param("status") String status, 
      @Param("startDate") LocalDateTime startDate, 
      @Param("endDate") LocalDateTime endDate);
  */
}

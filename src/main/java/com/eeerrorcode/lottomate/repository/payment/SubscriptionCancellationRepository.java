package com.eeerrorcode.lottomate.repository.payment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.eeerrorcode.lottomate.domain.entity.payment.CancellationType;
import com.eeerrorcode.lottomate.domain.entity.payment.SubscriptionCancellation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionCancellationRepository extends JpaRepository<SubscriptionCancellation, Long> {
  /**
   * 구독 ID로 취소 이력 조회
   * 
   * @param subscriptionId 구독 ID
   * @return 취소 이력 목록
   */
  List<SubscriptionCancellation> findBySubscriptionIdOrderByCancellationDateDesc(Long subscriptionId);
    
  /**
   * 사용자 ID로 취소 이력 조회
   * 
   * @param userId 사용자 ID
   * @return 취소 이력 목록
   */
  List<SubscriptionCancellation> findByUserIdOrderByCancellationDateDesc(Long userId);
    
  /**
   * 사용자 ID와 페이징으로 취소 이력 조회
   * 
   * @param userId 사용자 ID
   * @param pageable 페이징 정보
   * @return 페이징된 취소 이력
   */
  Page<SubscriptionCancellation> findByUserIdOrderByCancellationDateDesc(Long userId, Pageable pageable);
    
  /**
   * 취소 유형으로 취소 이력 조회
   * 
   * @param cancellationType 취소 유형
   * @return 취소 이력 목록
   */
  List<SubscriptionCancellation> findByCancellationTypeOrderByCancellationDateDesc(CancellationType cancellationType);
    
  /**
   * 환불 미처리 취소 이력 조회
   * 
   * @return 환불 미처리 취소 이력 목록
   */
  List<SubscriptionCancellation> findByRefundProcessedFalseAndCancellationTypeInOrderByCancellationDateAsc(
    List<CancellationType> cancellationTypes);
    
  /**
   * 관리자 미처리 취소 이력 조회
   * 
   * @return 관리자 미처리 취소 이력 목록
   */
  List<SubscriptionCancellation> findByAdminProcessedFalseOrderByCancellationDateAsc();
    
  /**
   * 특정 기간 내 취소 이력 조회
   * 
   * @param startDate 시작일
   * @param endDate 종료일
   * @return 취소 이력 목록
   */
  List<SubscriptionCancellation> findByCancellationDateBetweenOrderByCancellationDateDesc(
    LocalDateTime startDate, LocalDateTime endDate);
    
  /**
   * 구독 ID와 취소 유형으로 취소 이력 조회
   * 
   * @param subscriptionId 구독 ID
   * @param cancellationType 취소 유형
   * @return 취소 이력 (있으면)
   */
  Optional<SubscriptionCancellation> findBySubscriptionIdAndCancellationType(
    Long subscriptionId, CancellationType cancellationType);
    
  /**
   * 특정 사용자의 특정 취소 유형 건수 조회
   * 
   * @param userId 사용자 ID
   * @param cancellationType 취소 유형
   * @param startDate 시작일
   * @return 취소 건수
   */
  long countByUserIdAndCancellationTypeAndCancellationDateAfter(
    Long userId, CancellationType cancellationType, LocalDateTime startDate);
}

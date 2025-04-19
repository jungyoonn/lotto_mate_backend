package com.eeerrorcode.lottomate.domain.entity.payment;

import java.time.LocalDateTime;

import com.eeerrorcode.lottomate.domain.entity.common.BaseEntity;
import com.eeerrorcode.lottomate.domain.entity.user.User;

import jakarta.persistence.*;
import lombok.*;

@Entity(name = "subscription_cancellations")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
public class SubscriptionCancellation extends BaseEntity {
  // 구독 취소 이력 테이블
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "subscription_id", nullable = false)
  private Subscription subscription;
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;
  
  @Column(name = "cancellation_date", nullable = false)
  private LocalDateTime cancellationDate;
  
  @Column(name = "reason", nullable = true, columnDefinition = "text")
  private String reason;
  
  @Column(name = "effective_end_date", nullable = false)
  private LocalDateTime effectiveEndDate;
  
  @Column(name = "refund_processed", nullable = false, columnDefinition = "boolean default false")
  private boolean refundProcessed;
  
  @Column(name = "admin_processed", nullable = false, columnDefinition = "boolean default false")
  private boolean adminProcessed;
  
  @Column(name = "admin_note", nullable = true, columnDefinition = "text")
  private String adminNote;
  
  // 취소 유형 (즉시취소, 기간종료 후 취소, 환불 요청 등)
  @Enumerated(EnumType.STRING)
  @Column(name = "cancellation_type", nullable = false)
  private CancellationType cancellationType;
  
  /**
   * 환불 처리 상태 갱신
   * @param processed 환불 처리 여부
   */
  public void updateRefundProcessed(boolean processed) {
    this.refundProcessed = processed;
  }
  
  /**
   * 관리자 처리 상태 갱신
   * @param processed 관리자 처리 여부
   * @param note 관리자 노트
   */
  public void updateAdminProcessed(boolean processed, String note) {
    this.adminProcessed = processed;
    this.adminNote = note;
  }
}

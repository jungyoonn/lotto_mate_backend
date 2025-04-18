package com.eeerrorcode.lottomate.domain.entity.payment;

import com.eeerrorcode.lottomate.domain.entity.common.BaseEntity;
import com.eeerrorcode.lottomate.domain.entity.user.User;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity(name = "payments")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
public class Payment extends BaseEntity {
  // 결제 정보 테이블
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "subscription_id", nullable = true)
  private Subscription subscription;
  
  @Column(nullable = false)
  private BigDecimal amount;
  
  @Column(name = "payment_method", nullable = false)
  private String paymentMethod; // CARD, BANK, etc.
  
  @Enumerated(EnumType.STRING)
  @Column(name = "payment_status", nullable = false)
  private PaymentStatus paymentStatus;
  
  @Column(name = "merchant_uid", nullable = false, unique = true)
  private String merchantUid; // IAMPORT 주문번호
  
  @Column(name = "imp_uid", nullable = true)
  private String impUid; // IAMPORT 결제번호
  
  @Column(name = "payment_date", nullable = true)
  private LocalDateTime paymentDate;
  
  @Column(name = "refund_amount", nullable = false, columnDefinition = "decimal default 0")
  private BigDecimal refundAmount;
  
  @Column(name = "refund_date", nullable = true)
  private LocalDateTime refundDate;
  
  @Column(name = "pg_provider", nullable = true)
  private String pgProvider; // PG사 정보
  
  @Column(name = "card_name", nullable = true)
  private String cardName; // 카드사 정보
  
  @Column(name = "card_number", nullable = true)
  private String cardNumber; // 마스킹된 카드번호
  
  @Column(name = "bank_name", nullable = true)
  private String bankName; // 은행명
  
  @Column(name = "account_number", nullable = true)
  private String accountNumber; // 마스킹된 계좌번호
  
  @Column(name = "receipt_url", nullable = true)
  private String receiptUrl; // 영수증 URL
  
  /**
   * 결제 완료 처리
   * @param impUid 포트원 결제 고유번호
   * @param paymentDate 결제일시
   */
  public void completePayment(String impUid, LocalDateTime paymentDate) {
    this.impUid = impUid;
    this.paymentDate = paymentDate;
    this.paymentStatus = PaymentStatus.COMPLETE;
  }
  
  /**
   * 결제 실패 처리
   */
  public void failPayment() {
    this.paymentStatus = PaymentStatus.FAILED;
  }
  
  /**
   * 결제 보류 처리
   */
  public void pendingPayment() {
    this.paymentStatus = PaymentStatus.PENDING;
  }
  
  /**
   * 환불 처리
   * @param refundAmount 환불 금액
   * @param refundDate 환불 일시
   */
  public void refund(BigDecimal refundAmount, LocalDateTime refundDate) {
    this.refundAmount = refundAmount;
    this.refundDate = refundDate;
    this.paymentStatus = PaymentStatus.REFUNDED;
  }
  
  /**
   * 부분 환불 처리
   * @param partialRefundAmount 부분 환불 금액
   * @param refundDate 환불 일시
   */
  public void partialRefund(BigDecimal partialRefundAmount, LocalDateTime refundDate) {
    this.refundAmount = partialRefundAmount;
    this.refundDate = refundDate;
    if (partialRefundAmount.compareTo(this.amount) < 0) {
      this.paymentStatus = PaymentStatus.PARTIAL_REFUNDED;
    } else {
      this.paymentStatus = PaymentStatus.REFUNDED;
    }
  }
  
  /**
   * 영수증 URL 설정
   * @param receiptUrl 영수증 URL
   */
  public void setReceiptUrl(String receiptUrl) {
    this.receiptUrl = receiptUrl;
  }
  
  /**
   * 카드 정보 설정
   * @param cardName 카드사 이름
   * @param cardNumber 마스킹된 카드 번호
   */
  public void setCardInfo(String cardName, String cardNumber) {
    this.cardName = cardName;
    this.cardNumber = cardNumber;
  }
  
  /**
   * 은행 정보 설정
   * @param bankName 은행명
   * @param accountNumber 마스킹된 계좌번호
   */
  public void setBankInfo(String bankName, String accountNumber) {
    this.bankName = bankName;
    this.accountNumber = accountNumber;
  }
  
  /**
   * PG사 정보 설정
   * @param pgProvider PG사 정보
   */
  public void setPgProvider(String pgProvider) {
    this.pgProvider = pgProvider;
  }
  
  /**
   * 구독 정보 연결
   * @param subscription 구독 정보
   */
  public void linkSubscription(Subscription subscription) {
    this.subscription = subscription;
  }
  
  /**
   * 결제가 완료되었는지 확인
   * @return 결제 완료 여부
   */
  public boolean isCompleted() {
    return this.paymentStatus == PaymentStatus.COMPLETE;
  }
  
  /**
   * 결제가 환불되었는지 확인
   * @return 환불 여부
   */
  public boolean isRefunded() {
    return this.paymentStatus == PaymentStatus.REFUNDED || 
      this.paymentStatus == PaymentStatus.PARTIAL_REFUNDED;
  }
}
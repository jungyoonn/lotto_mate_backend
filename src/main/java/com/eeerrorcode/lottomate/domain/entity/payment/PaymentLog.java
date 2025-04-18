package com.eeerrorcode.lottomate.domain.entity.payment;

import com.eeerrorcode.lottomate.domain.entity.common.BaseEntity;
import com.eeerrorcode.lottomate.domain.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

@Entity(name = "payment_logs")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
public class PaymentLog extends BaseEntity {
  // 결제 로그 테이블
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "payment_id", nullable = true)
  private Payment payment;

  @Enumerated(EnumType.STRING)
  @Column(name = "action", nullable = false)
  private PaymentLogAction action;

  @Column(name = "request_data", columnDefinition = "text", nullable = true)
  private String requestData;  // 요청 데이터 (JSON)

  @Column(name = "response_data", columnDefinition = "text", nullable = true)
  private String responseData;  // 응답 데이터 (JSON)

  @Column(name = "ip_address", nullable = false)
  private String ipAddress;

  /**
  * 요청 데이터 설정
  * @param requestData JSON 형식의 요청 데이터
  */
  public void setRequestData(String requestData) {
    this.requestData = requestData;
  }

  /**
  * 응답 데이터 설정
  * @param responseData JSON 형식의 응답 데이터
  */
  public void setResponseData(String responseData) {
    this.responseData = responseData;
  }

  /**
  * 결제 로그에 결제 정보 연결
  * @param payment 결제 정보
  */
  public void linkPayment(Payment payment) {
    this.payment = payment;
  }
}
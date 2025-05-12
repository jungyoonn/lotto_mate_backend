package com.eeerrorcode.lottomate.domain.dto.subscription;

import java.math.BigDecimal;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionVerifyPaymentRequestDto {
  // 결제가 완료되면 검증과 구독 활성화를 위해 사용, 검증이 완료되면 구독이 활성화됨

  @NotBlank(message = "포트원 결제 고유번호는 필수입니다")
  private String impUid;       // 포트원 결제 고유번호
  
  @NotBlank(message = "주문번호는 필수입니다")
  private String merchantUid;  // 주문번호
  
  @NotBlank(message = "구독 플랜은 필수입니다")
  private String plan;         // 구독 플랜 (basic, standard, premium)
  
  @NotBlank(message = "구독 기간은 필수입니다")
  private String period;       // 구독 기간 (monthly, yearly)
  
  @NotNull(message = "결제 금액은 필수입니다")
  private BigDecimal amount;   // 결제 금액
  
  @Email(message = "올바른 이메일 형식이 아닙니다")
  private String userEmail;    // 사용자 이메일 (인증이 없을 때 사용)
}

package com.eeerrorcode.lottomate.domain.dto.payment;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentLogResponseDto {
  private Long id;
  private String action;
  private String paymentId;
  private String formattedDate;
  private String ipAddress;
  
  // 관리자용 추가 정보
  private String requestData;
  private String responseData;
  private Long userId;

  /**
   * 날짜 포맷팅
   * @param dateTime 포맷팅할 날짜시간
   */
  public void setFormattedDateFromDateTime(LocalDateTime dateTime) {
    if (dateTime != null) {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
      this.formattedDate = dateTime.format(formatter);
    }
  }
}
